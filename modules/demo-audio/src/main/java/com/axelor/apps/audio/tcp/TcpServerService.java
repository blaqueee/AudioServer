package com.axelor.apps.audio.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class TcpServerService {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerService.class);
    private static final int SERVER_PORT = 9000;

    private ServerSocket serverSocket;
    private final ExecutorService connectionAcceptorExecutor;
    private final ExecutorService clientHandlerExecutor;
    private final ScheduledExecutorService cleanupExecutor;
    private final TcpSessionStorage tcpSessionStorage;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Inject
    public TcpServerService(TcpSessionStorage tcpSessionStorage) {
        this.tcpSessionStorage = tcpSessionStorage;
        this.connectionAcceptorExecutor = Executors.newSingleThreadExecutor();
        this.clientHandlerExecutor = Executors.newCachedThreadPool();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        this.init();
    }

    public void init() {
        logger.info("TcpServerService init method called. Starting TCP server.");
        if (running.compareAndSet(false, true)) {
            connectionAcceptorExecutor.submit(this::startServer);
        }
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            logger.info("TCP Server started and listening on port {}. Waiting for clients...", SERVER_PORT);

            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New TCP client connected from: {}. Waiting for client ID...", clientSocket.getRemoteSocketAddress());
                    clientHandlerExecutor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running.get()) {
                        logger.error("Error accepting client connection: {}. Retrying...", e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Could not start TCP server on port {}: {}", SERVER_PORT, e.getMessage(), e);
        } finally {
            closeServerSocket();
            running.set(false);
            logger.info("TCP Server stopped.");
        }
    }

    /**
     * Обрабатывает новое входящее TCP-соединение.
     * Ожидает, что клиент отправит свой ID первой строкой.
     */
    private void handleClient(Socket clientSocket) {
        String clientId = null;
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            clientSocket.setSoTimeout(5000);

            clientId = reader.readLine();
            clientSocket.setSoTimeout(0);

            if (clientId == null || clientId.trim().isEmpty()) {
                logger.warn("Client {} connected but did not provide a valid ID. Closing connection.", clientSocket.getRemoteSocketAddress());
                clientSocket.close();
                return;
            }
            clientId = clientId.trim();

            if (tcpSessionStorage.containsClient(clientId)) {
                logger.warn("Duplicate client ID received: {}. Existing client connection might be replaced.", clientId);
            }

            tcpSessionStorage.addClient(clientId, outputStream);
            logger.info("TCP client '{}' connected and registered from {}. Total active TCP clients: {}",
                    clientId, clientSocket.getRemoteSocketAddress(), tcpSessionStorage.getConnectedClients().size());

            while (running.get() && !clientSocket.isClosed() && clientSocket.isConnected()) {
                try {
                    int byteRead = clientSocket.getInputStream().read();
                    if (byteRead == -1) {
                        logger.info("Client '{}' closed connection gracefully (EOF).", clientId);
                        break;
                    }

                    logger.debug("Received unexpected byte from client '{}': {}", clientId, byteRead);

                } catch (IOException e) {
                    logger.info("Client '{}' connection error (likely disconnected): {}", clientId, e.getMessage());
                    break;
                }
            }

        } catch (IOException e) {
            logger.error("Error handling client connection for {}: {}", (clientId != null ? clientId : clientSocket.getRemoteSocketAddress()), e.getMessage(), e); // Стек-трейс
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                logger.warn("Error closing client socket for {}: {}", clientId, e.getMessage());
            }
            if (clientId != null) {
                tcpSessionStorage.removeClient(clientId);
                logger.info("TCP client '{}' disconnected. Total active TCP clients: {}", clientId, tcpSessionStorage.getConnectedClients().size());
            }
        }
    }

    @PreDestroy
    public void close() {
        logger.info("Closing TcpServerService. Shutting down executors and all client connections.");
        running.set(false);

        shutdownExecutor(connectionAcceptorExecutor, "Connection Acceptor");
        shutdownExecutor(clientHandlerExecutor, "Client Handler");
        shutdownExecutor(cleanupExecutor, "Cleanup");

        closeServerSocket();

        for (OutputStream os : tcpSessionStorage.getConnectedClients().values()) {
            try {
                os.close();
            } catch (IOException e) {
                logger.warn("Error closing client output stream: {}", e.getMessage());
            }
        }
        tcpSessionStorage.getConnectedClients().clear();
        logger.info("TcpServerService closed and all resources released.");
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("{} executor did not terminate in time, forcing shutdown.", name);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warn("{} executor interrupted during shutdown.", name);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                logger.info("ServerSocket closed.");
            }
        } catch (IOException e) {
            logger.error("Error closing ServerSocket: {}", e.getMessage());
        } finally {
            serverSocket = null;
        }
    }
}