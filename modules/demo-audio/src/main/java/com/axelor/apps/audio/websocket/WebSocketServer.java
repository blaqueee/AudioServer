package com.axelor.apps.audio.websocket;

import com.axelor.apps.audio.websocket.config.NoAuthWebSocketConfigurator;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.web.socket.Channel;
import com.axelor.web.socket.MessageDecoder;
import com.axelor.web.socket.MessageEncoder;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@ServerEndpoint(
        value = "/web-socket-endpoint",
        decoders = {MessageDecoder.class},
        encoders = {MessageEncoder.class},
        configurator = NoAuthWebSocketConfigurator.class
)
public class WebSocketServer {

    private static final Map<String, Channel> CHANNELS = new ConcurrentHashMap();
    private final SessionStorage sessionStorage;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    public WebSocketServer(Set<Channel> channels) {
        channels.stream().filter(Channel::isEnabled).forEach(this::register);
        this.sessionStorage = Beans.get(SessionStorage.class);
    }

    private void register(Channel channel) {
        String name = channel.getName();
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Channel must have a name: " + channel.getClass().getName());
        } else if (CHANNELS.containsKey(name)) {
            throw new IllegalStateException("Duplicate channel found: " + name);
        } else {
            LOG.info("Registering channel: {}", name);
            CHANNELS.put(name, channel);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        Map<String, List<String>> params = session.getRequestParameterMap();
        List<String> ides = params.getOrDefault("id", null);

        if (ides != null && !ides.isEmpty()) {
            String id = ides.get(0);
            sessionStorage.addSession(session, Long.parseLong(id));
            LOG.debug("Client connected: {}", id);
        } else {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "ID required"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOG.error("Client disconnected: {}", session.getId());
        sessionStorage.removeSession(session.getId());

        if (session.isOpen()) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Session closed"));
            } catch (IOException e) {
                LOG.error("Failed to close session after error: {}", session.getId(), e);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOG.error("WebSocket error on session {}: {}", session.getId(), throwable.getMessage(), throwable);
        sessionStorage.removeSession(session.getId());

        if (session.isOpen()) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Server error"));
            } catch (IOException e) {
                LOG.error("Failed to close session after error: {}", session.getId(), e);
            }
        }
    }

}