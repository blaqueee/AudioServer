package com.axelor.apps.audio.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Singleton
public class TcpSessionStorage {
    private final ConcurrentHashMap<String, OutputStream> connectedClients = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(TcpSessionStorage.class);
    private final ExecutorService clientHandlerExecutor;

    @Inject
    public TcpSessionStorage(ExecutorService clientHandlerExecutor) {
        this.clientHandlerExecutor = clientHandlerExecutor;
    }

    public void addClient(String clientId, OutputStream out) {
        connectedClients.put(clientId, out);
    }

    public ConcurrentHashMap<String, OutputStream> getConnectedClients() {
        return connectedClients;
    }

    public void removeClient(String clientId) {
        connectedClients.remove(clientId);
    }

    public OutputStream getClient(String clientId) {
        return connectedClients.get(clientId);
    }

    public boolean containsClient(String clientId) {
        return connectedClients.containsKey(clientId);
    }

    public void sendBytes(ByteBuffer data, List<String> targetClientIDs) {
        if (targetClientIDs == null || targetClientIDs.isEmpty()) {
            logger.warn("No target client IDs provided for sending data. Skipping TCP send.");
            return;
        }

        final byte[] bytesToSend = new byte[data.remaining()];
        data.duplicate().get(bytesToSend);

        for (String clientId : targetClientIDs) {
            OutputStream outputStream = this.getClient(clientId);
            if (outputStream == null) {
                logger.warn("TCP client '{}' is not connected or not found. Skipping send.", clientId);
                continue;
            }

            clientHandlerExecutor.submit(() -> {
                try {
                    outputStream.write(bytesToSend);
                    outputStream.flush();
                    logger.debug("Sent {} bytes to TCP client '{}'.", bytesToSend.length, clientId);
                } catch (IOException e) {
                    logger.error("Error sending bytes to TCP client '{}': {}. Removing client.", clientId, e.getMessage(), e);
                    this.removeClient(clientId);
                } catch (Exception e) {
                    logger.error("Unexpected error during send to TCP client '{}': {}", clientId, e.getMessage(), e);
                }
            });
        }
    }
}
