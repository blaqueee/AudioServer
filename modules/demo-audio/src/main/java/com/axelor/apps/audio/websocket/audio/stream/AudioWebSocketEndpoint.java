package com.axelor.apps.audio.websocket.audio.stream;

import com.axelor.apps.audio.tcp.TcpSessionStorage;
import com.axelor.apps.audio.websocket.config.NoAuthWebSocketConfigurator;
import com.axelor.common.StringUtils;
import com.axelor.web.socket.Channel;
import com.axelor.web.socket.MessageDecoder;
import com.axelor.web.socket.MessageEncoder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
@ServerEndpoint(
        value = "/audio-stream",
        decoders = {MessageDecoder.class},
        encoders = {MessageEncoder.class},
        configurator = NoAuthWebSocketConfigurator.class
)
public class AudioWebSocketEndpoint {

    private static final Map<String, Channel> CHANNELS = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(AudioWebSocketEndpoint.class);
    private final AudioSessionStorage audioSessionStorage;
    private final TcpSessionStorage tcpSessionStorage;

    private Process ffmpegProcess = null;
    private OutputStream ffmpegStdin = null;
    private ExecutorService ffmpegOutputReaderExecutor = null;
    private ExecutorService ffmpegErrorReaderExecutor = null;
    private final Object ffmpegLock = new Object();


    @Inject
    public AudioWebSocketEndpoint(Set<Channel> channels, AudioSessionStorage audioSessionStorage, TcpSessionStorage tcpSessionStorage) {
        channels.stream().filter(Channel::isEnabled).forEach(this::register);
        this.audioSessionStorage = audioSessionStorage;
        this.tcpSessionStorage = tcpSessionStorage;
        logger.info("AudioWebSocketEndpoint initialized.");
    }

    private void register(Channel channel) {
        ProcessBuilder builder = new ProcessBuilder("node", "server.js");
//        builder.directory(new File())


        String name = channel.getName();
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Channel must have a name: " + channel.getClass().getName());
        } else if (CHANNELS.containsKey(name)) {
            throw new IllegalStateException("Duplicate channel found: " + name);
        } else {
            logger.info("Registering channel: {}", name);
            CHANNELS.put(name, channel);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        audioSessionStorage.addSession(session);
        Map<String, List<String>> requestParameterMap = session.getRequestParameterMap();
        logger.info("DEBUG: onOpen: Session ID: {}", session.getId());

        List<String> rawClientIdsFromUrl = requestParameterMap.get("tcpClients");

        List<String> parsedClientIds = new ArrayList<>();

        if (rawClientIdsFromUrl != null && !rawClientIdsFromUrl.isEmpty()) {
            String clientIdsString = rawClientIdsFromUrl.get(0);
            if (!clientIdsString.isEmpty()) {
                parsedClientIds.addAll(Arrays.asList(clientIdsString.split(",")));
            }

            session.getUserProperties().put("clientIdsForSession", parsedClientIds);
            logger.info("WebSocket opened: {}. Client IDs from URL: {}. Total sessions: {}",
                    session.getId(), parsedClientIds, audioSessionStorage.getSessions().size());
        } else {
            logger.warn("WebSocket opened: {}. No 'tcpClients' parameter found in URL. Total sessions: {}",
                    session.getId(), audioSessionStorage.getSessions().size());
        }
    }

    @OnClose
    public void onClose(Session session) {
        audioSessionStorage.removeSession(session);
        logger.info("WebSocket closed: {}. Total sessions: {}", session.getId(), audioSessionStorage.getSessions().size());
        cleanupFfmpegResources();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("Received text message from {}: {}", session.getId(), message);
    }

    @OnMessage
    public void onMessage(ByteBuffer message, Session session) {
        @SuppressWarnings("unchecked")
        List<String> targetTcpClientIDs = (List<String>) session.getUserProperties().get("clientIdsForSession");

        synchronized (ffmpegLock) {
            if (ffmpegProcess == null || !ffmpegProcess.isAlive()) {
                logger.info("FFmpeg process is not running. Starting FFmpeg...");
                try {
                    ffmpegOutputReaderExecutor = Executors.newSingleThreadExecutor();
                    ffmpegErrorReaderExecutor = Executors.newSingleThreadExecutor();

                    startFfmpegProcess(targetTcpClientIDs);
                } catch (IOException e) {
                    logger.error("Failed to start FFmpeg process: {}. Please ensure FFmpeg is in your PATH or correctly placed in the project's 'bin/ffmpeg/' directory.", e.getMessage(), e);
                    cleanupFfmpegResources();
                    return;
                }
            }
        }

        try {
            if (ffmpegStdin != null) {
                ffmpegStdin.write(message.array(), message.position(), message.remaining());
                ffmpegStdin.flush();
            } else {
                logger.warn("FFmpeg stdin is null. Cannot pipe PCM data.");
            }
        } catch (IOException e) {
            logger.error("Error writing PCM to FFmpeg stdin: {}. FFmpeg process might have crashed or disconnected.", e.getMessage());
            cleanupFfmpegResources();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("WebSocket error for session {}: {}", session.getId(), throwable.getMessage(), throwable);
        audioSessionStorage.removeSession(session);
        cleanupFfmpegResources();
    }

    private void startFfmpegProcess(List<String> targetTcpClientIDs) throws IOException {
        String ffmpegExecutable = "ffmpeg";
        logger.info("Attempting to start FFmpeg.");

        targetTcpClientIDs.forEach(System.out::println);

        ProcessBuilder pb = new ProcessBuilder(
                ffmpegExecutable,
                "-f", "s16le",
                "-ar", String.valueOf(48000),
                "-ac", String.valueOf(1),
                "-i", "pipe:0",
                "-c:a", "pcm_s16le",
                "-ar", String.valueOf(48000),
                "-ac", String.valueOf(1),
                "-f", "s16le",
                "pipe:1"
        );

        ffmpegProcess = pb.start();
        ffmpegStdin = ffmpegProcess.getOutputStream();
        InputStream ffmpegStdout = ffmpegProcess.getInputStream();
        InputStream ffmpegStderr = ffmpegProcess.getErrorStream();

        logger.info("FFmpeg process started successfully.");

        if (ffmpegOutputReaderExecutor == null || ffmpegOutputReaderExecutor.isShutdown()) {
            ffmpegOutputReaderExecutor = Executors.newSingleThreadExecutor();
        }
        if (ffmpegErrorReaderExecutor == null || ffmpegErrorReaderExecutor.isShutdown()) {
            ffmpegErrorReaderExecutor = Executors.newSingleThreadExecutor();
        }

        ffmpegOutputReaderExecutor.submit(() -> {
            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                logger.info("Starting FFmpeg stdout reader thread...");
                while ((bytesRead = ffmpegStdout.read(buffer)) != -1 && !Thread.currentThread().isInterrupted()) {
                    if (bytesRead > 0) {
                        byte[] dataToSend = new byte[bytesRead];
                        System.arraycopy(buffer, 0, dataToSend, 0, bytesRead);
                        tcpSessionStorage.sendBytes(ByteBuffer.wrap(dataToSend), targetTcpClientIDs);
                    }
                }
                logger.info("FFmpeg stdout stream ended. FFmpeg process likely terminated.");
            } catch (IOException e) {
                logger.error("Error reading from FFmpeg stdout: {}. FFmpeg process might have crashed or disconnected.", e.getMessage());
            } finally {
                logger.info("FFmpeg stdout reader thread shutting down.");
            }
        });

        ffmpegErrorReaderExecutor.submit(() -> {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(ffmpegStderr, StandardCharsets.UTF_8))) {
                String line;
                logger.info("Starting FFmpeg stderr reader thread...");
                while ((line = errorReader.readLine()) != null) {
                    logger.warn("FFmpeg STDERR: {}", line);
                }
                logger.info("FFmpeg stderr stream ended.");
            } catch (IOException e) {
                logger.error("Error reading FFmpeg stderr: {}", e.getMessage());
            } finally {
                logger.info("FFmpeg stderr reader thread shutting down.");
            }
        });
    }

    @PreDestroy
    private void cleanupFfmpegResources() {
        synchronized (ffmpegLock) {
            if (ffmpegStdin != null) {
                try {
                    ffmpegStdin.close();
                } catch (IOException e) {
                    logger.warn("Error closing FFmpeg stdin: {}", e.getMessage());
                }
                ffmpegStdin = null;
            }
            if (ffmpegProcess != null) {
                logger.info("Destroying FFmpeg process.");
                ffmpegProcess.destroy();
                try {
                    if (!ffmpegProcess.waitFor(5, TimeUnit.SECONDS)) {
                        ffmpegProcess.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    logger.warn("FFmpeg process destruction interrupted.", e);
                    Thread.currentThread().interrupt();
                    ffmpegProcess.destroyForcibly();
                }
                ffmpegProcess = null;
            }

            if (ffmpegOutputReaderExecutor != null && !ffmpegOutputReaderExecutor.isShutdown()) {
                ffmpegOutputReaderExecutor.shutdown();
                try {
                    if (!ffmpegOutputReaderExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        ffmpegOutputReaderExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    ffmpegOutputReaderExecutor.shutdownNow();
                }
            }
            logger.info("FFmpeg resources cleaned up.");
        }
    }
}