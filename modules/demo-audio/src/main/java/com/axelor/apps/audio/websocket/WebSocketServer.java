package com.axelor.apps.audio.websocket;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/ws")
public class WebSocketServer {

    private final SessionStorage sessionStorage;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    public WebSocketServer(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @OnOpen
    public void onOpen(Session session) {
        Map<String, List<String>> params = session.getRequestParameterMap();
        String id = params.getOrDefault("id", null).toString();

        if (id != null) {
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
        sessionStorage.removeSession(session.getId());
    }
}