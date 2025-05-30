package com.axelor.apps.audio.websocket.channesl;

import com.axelor.web.socket.Channel;
import com.axelor.web.socket.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.lang.invoke.MethodHandles;

public class EvenLogChanel extends Channel {
    private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String NAME = "eventLog";

    @Override
    public String getName() {
            return NAME;
    }

    @Override
    public void onMessage(Session session, Message message) {
        log.debug(message.getChannel());
    }
}
