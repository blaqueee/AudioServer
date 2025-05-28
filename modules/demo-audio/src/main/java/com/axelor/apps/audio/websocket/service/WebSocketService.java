package com.axelor.apps.audio.websocket.service;

import com.axelor.apps.audio.websocket.dto.CommandDto;

public interface WebSocketService {
    void sendTo(CommandDto commandDto) throws Exception;
}
