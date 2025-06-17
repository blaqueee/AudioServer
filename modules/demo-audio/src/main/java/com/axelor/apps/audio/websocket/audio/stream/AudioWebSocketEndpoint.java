package com.axelor.apps.audio.websocket.audio.stream;

import com.axelor.apps.audio.tcp.TcpSessionStorage;
import com.axelor.apps.audio.websocket.config.NoAuthWebSocketConfigurator;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@ServerEndpoint(
        value = "/audio",
        decoders = {MessageDecoder.class},
        encoders = {MessageEncoder.class},
        configurator = NoAuthWebSocketConfigurator.class
)
public class AudioWebSocketEndpoint {

    private static final Map<String, Channel> CHANNELS = new ConcurrentHashMap();
    private static final Logger logger = LoggerFactory.getLogger(AudioWebSocketEndpoint.class);
    private final AudioSessionStorage audioSessionStorage;
    private final TcpSessionStorage tcpSessionStorage;

    @Inject
    public AudioWebSocketEndpoint(Set<Channel> channels) {
        channels.stream().filter(Channel::isEnabled).forEach(this::register);
        this.audioSessionStorage = Beans.get(AudioSessionStorage.class);
        this.tcpSessionStorage = Beans.get(TcpSessionStorage.class);
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

        List<String> clientIdsFromUrl = requestParameterMap.get("tcpClients");

        if (clientIdsFromUrl != null && !clientIdsFromUrl.isEmpty()) {
            session.getUserProperties().put("clientIdsForSession", clientIdsFromUrl);
            logger.info("WebSocket opened: {}. Client IDs from URL: {}. Total sessions: {}",
                    session.getId(), clientIdsFromUrl, audioSessionStorage.getSessions().size());
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
        logger.debug("Received {} bytes of binary audio from session {}", message.remaining(), session.getId());

        @SuppressWarnings("unchecked")
        List<String> targetTcpClientIDs = (List<String>) session.getUserProperties().get("clientIdsForSession");

        if (targetTcpClientIDs != null && !targetTcpClientIDs.isEmpty()) {
            logger.info("Processing audio for session {} with IDs: {}", session.getId(), targetTcpClientIDs);
            tcpSessionStorage.sendBytes(message, targetTcpClientIDs);

        } else {
            logger.warn("Received audio for session {} but no target TCP client IDs were set or found. Skipping TCP send.", session.getId());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("WebSocket error for session {}: {}", session.getId(), throwable.getMessage(), throwable);
        audioSessionStorage.removeSession(session);
    }
}