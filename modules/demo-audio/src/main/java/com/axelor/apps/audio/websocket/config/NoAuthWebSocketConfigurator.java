package com.axelor.apps.audio.websocket.config;

import com.axelor.inject.Beans;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class NoAuthWebSocketConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        // Accept all connections, do nothing
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) {
        return Beans.get(endpointClass); // or newInstance if DI not needed
    }

}
