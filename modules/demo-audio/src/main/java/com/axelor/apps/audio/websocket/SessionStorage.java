package com.axelor.apps.audio.websocket;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SessionStorage {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public void addSession(Session session, String customsOCode) {
        sessions.put(customsOCode, session);
    }

    public void removeSession(String sessionId) {
        sessions.values().removeIf(s -> s.getId().equals(sessionId));
    }

    public Session getSession(String customsOCode) {
        return sessions.get(customsOCode);
    }

    public void sendTo(String code, String message) throws Exception {
        Session session = getSession(code);
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
