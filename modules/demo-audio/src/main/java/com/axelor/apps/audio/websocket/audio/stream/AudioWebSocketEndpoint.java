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

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    @Inject
    public AudioWebSocketEndpoint(Set<Channel> channels, AudioSessionStorage audioSessionStorage, TcpSessionStorage tcpSessionStorage) {
        channels.stream().filter(Channel::isEnabled).forEach(this::register);
        this.audioSessionStorage = audioSessionStorage;
        this.tcpSessionStorage = tcpSessionStorage;
        logger.info("AudioWebSocketEndpoint initialized.");
    }

    private void register(Channel channel) {
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
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("Received text message from {}: {}", session.getId(), message);
    }

    @OnMessage
    public void onMessage(ByteBuffer message, Session session) {
        @SuppressWarnings("unchecked")
        List<String> targetTcpClientIDs = (List<String>) session.getUserProperties().get("clientIdsForSession");

        try {
            if (targetTcpClientIDs != null && !targetTcpClientIDs.isEmpty()) {
                tcpSessionStorage.sendBytes(message, targetTcpClientIDs);
            } else {
                logger.warn("No 'tcpClients' specified for session {}. Cannot pipe PCM data to TCP.", session.getId());
            }
        } catch (Exception e) {
            logger.error("Error sending PCM to TCP clients for session {}: {}", session.getId(), e.getMessage(), e);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("WebSocket error for session {}: {}", session.getId(), throwable.getMessage(), throwable);
        audioSessionStorage.removeSession(session);
    }
}