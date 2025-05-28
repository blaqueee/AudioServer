package com.axelor.apps.audio.websocket.service.impl;

import com.axelor.apps.audio.websocket.WebSocketServer;
import com.axelor.apps.audio.websocket.dto.CommandDto;
import com.axelor.apps.audio.websocket.service.WebSocketService;

public class WebSocketServiceImpl implements WebSocketService {
    @Override
    public void sendTo(CommandDto commandDto) throws Exception {
        WebSocketServer.sendTo("", commandDto.getUrl());
    }
}
