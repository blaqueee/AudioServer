package com.axelor.apps.audio.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class TcpSessionStorage {
    private final ConcurrentHashMap<String, OutputStream> connectedClients = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(TcpSessionStorage.class);
    private final ExecutorService clientHandlerExecutor;

    @Inject
    public TcpSessionStorage() {
        this.clientHandlerExecutor = Executors.newCachedThreadPool();
    }

    public void addClient(String clientId, OutputStream out) {
        connectedClients.put(clientId, out);
        logger.info("TCP client '{}' added. Total active TCP clients: {}", clientId, connectedClients.size());
    }

    public ConcurrentHashMap<String, OutputStream> getConnectedClients() {
        return connectedClients;
    }

    public void removeClient(String clientId) {
        OutputStream outputStream = connectedClients.remove(clientId);
        if (outputStream != null) {
            try {
                outputStream.close();
                logger.info("TCP client '{}' removed and OutputStream closed. Total active TCP clients: {}", clientId, connectedClients.size());
            } catch (IOException e) {
                logger.warn("Error closing OutputStream for client '{}' during removal: {}", clientId, e.getMessage());
            }
        } else {
            logger.warn("Attempted to remove non-existent client '{}'.", clientId);
        }
    }

    public boolean containsClient(String clientId) {
        return connectedClients.containsKey(clientId);
    }

    public void sendBytes(ByteBuffer data, List<String> targetTcpClientIDs) {
        if (connectedClients.isEmpty()) {
            logger.debug("sendBytes: No TCP clients connected. Skipping send.");
            return;
        }

        if (targetTcpClientIDs == null || targetTcpClientIDs.isEmpty()) {
            logger.debug("sendBytes: No target TCP client IDs specified. Skipping send.");
            return;
        }

        final byte[] bytesToSend = new byte[data.remaining()];
        data.duplicate().get(bytesToSend);

        for (Map.Entry<String, OutputStream> entry : connectedClients.entrySet()) {
            String clientId = entry.getKey();
            OutputStream outputStream = entry.getValue();

            if (targetTcpClientIDs.contains(clientId)) {
                clientHandlerExecutor.submit(() -> {
                    try {
                        outputStream.write(bytesToSend);
                        outputStream.flush();
                    } catch (IOException e) {
                        logger.error("Error sending bytes to client '{}': {}. Removing client.", clientId, e.getMessage());
                        removeClient(clientId);
                    } catch (Exception e) {
                        logger.error("Unexpected error during send to client '{}': {}", clientId, e.getMessage(), e);
                    }
                });
            }
        }
    }
}