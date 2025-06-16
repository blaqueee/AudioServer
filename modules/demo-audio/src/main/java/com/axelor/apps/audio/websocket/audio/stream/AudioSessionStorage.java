package com.axelor.apps.audio.websocket.audio.stream;

import javax.inject.Singleton;
import javax.websocket.Session;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class AudioSessionStorage {

    private static final Set<Session> activeSessions = Collections.synchronizedSet(new HashSet<>());

    public void addSession(Session session) {
        activeSessions.add(session);
    }

    public void removeSession(Session session) {
        activeSessions.remove(session);
    }

    public Set<Session> getSessions(){
        return activeSessions;
    }
}
