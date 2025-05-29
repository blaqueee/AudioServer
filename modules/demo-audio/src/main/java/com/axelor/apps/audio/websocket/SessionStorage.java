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
    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    public void addSession(Session session, Long id) {
        sessions.put(id, session);
    }

    public void removeSession(String sessionId) {
        sessions.values().removeIf(s -> s.getId().equals(sessionId));
    }

    public Session getSession(Long id) {
        return sessions.get(id);
    }

    public void sendTo(Long id, String message) throws Exception {
        Session session = getSession(id);
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
