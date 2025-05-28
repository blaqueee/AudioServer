package com.axelor.apps.audio.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws")
public class WebSocketServer {

    private static final Map<String, Session> clients = new ConcurrentHashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @OnOpen
    public void onOpen(Session session) {
        Map<String, List<String>> params = session.getRequestParameterMap();
        String code = params.getOrDefault("code", List.of()).stream().findFirst().orElse(null);

        if (code != null) {
            clients.put(code, session);
            LOG.debug("Client connected: {}", code);
        } else {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Code required"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        clients.values().removeIf(s -> s.getId().equals(session.getId()));
    }

    public static void sendTo(String code, String message) throws Exception {
        Session session = clients.get(code);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                LOG.debug("Message sent: {}", message);
            } catch (IOException e) {
                throw new Exception();
            }
        } else {
            throw new Exception("Client not found");
        }
    }
}