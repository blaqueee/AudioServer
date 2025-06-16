package com.axelor.apps.audio.tcp; // Ваш пакет

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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

    private final ConcurrentHashMap<String, OutputStream> connectedClients = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Inject
    public TcpServerService() {
        System.out.println("DEBUG: TcpServerService constructor called."); // <-- ТРАССИРОВКА
        this.connectionAcceptorExecutor = Executors.newSingleThreadExecutor();
        this.clientHandlerExecutor = Executors.newCachedThreadPool();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

        logger.info("TcpServerService initialized. Will listen on port {}.", SERVER_PORT);
    }

    @PostConstruct
    public void init() {
        System.out.println("DEBUG: TcpServerService @PostConstruct init method called."); // <-- ТРАССИРОВКА
        logger.info("TcpServerService init method called. Starting TCP server.");
        if (running.compareAndSet(false, true)) {
            connectionAcceptorExecutor.submit(this::startServer);
        }
    }

    private void startServer() {
        System.out.println("DEBUG: Entering startServer method."); // <-- ТРАССИРОВКА
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            logger.info("TCP Server started and listening on port {}. Waiting for clients...", SERVER_PORT);
            System.out.println("DEBUG: ServerSocket successfully bound to port " + SERVER_PORT); // <-- ТРАССИРОВКА

            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New TCP client connected from: {}. Waiting for client ID...", clientSocket.getRemoteSocketAddress());
                    clientHandlerExecutor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running.get()) {
                        logger.error("Error accepting client connection: {}. Retrying...", e.getMessage());
                        // try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); } // Убрал паузу для быстрых логов
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not start TCP server on port " + SERVER_PORT + ": " + e.getMessage()); // <-- ТРАССИРОВКА
            logger.error("Could not start TCP server on port {}: {}", SERVER_PORT, e.getMessage(), e); // Логирование с полным стеком
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
            // Установка таймаута для чтения ID, чтобы не зависнуть, если клиент не отправит ID
            clientSocket.setSoTimeout(5000); // 5 секунд на отправку ID

            clientId = reader.readLine();
            clientSocket.setSoTimeout(0); // Сброс таймаута после чтения ID

            if (clientId == null || clientId.trim().isEmpty()) {
                logger.warn("Client {} connected but did not provide a valid ID. Closing connection.", clientSocket.getRemoteSocketAddress());
                clientSocket.close();
                return;
            }
            clientId = clientId.trim();

            if (connectedClients.containsKey(clientId)) {
                logger.warn("Duplicate client ID received: {}. Existing client connection might be replaced.", clientId);
                // В идеале, если нужна одна сессия на ID, старую нужно закрыть или отказать в новой.
                // В текущей реализации - новая сессия просто перепишет OutputStream, старая не закроется явно.
            }

            connectedClients.put(clientId, outputStream); // Сохраняем OutputStream для этого клиента
            logger.info("TCP client '{}' connected and registered from {}. Total active TCP clients: {}",
                    clientId, clientSocket.getRemoteSocketAddress(), connectedClients.size());

            // Этот цикл будет блокироваться, пока клиент не отключится.
            // Если вы хотите читать данные ОТ TCP-клиента, то здесь нужна логика чтения.
            // Если вы только отправляете, то этот цикл может быть пустым или просто ждать EOF.
            while (running.get() && !clientSocket.isClosed() && clientSocket.isConnected()) {
                // Если нет входящих данных от TCP-клиента, можно сделать небольшой таймаут
                // или реализовать более сложную логику проверки "живости"
                try {
                    // Просто ждем, пока сокет не будет закрыт или не произойдет ошибка.
                    // Если TCP-клиент не отправляет данные, этот поток будет блокироваться здесь.
                    // Это может быть нормально, если TCP-клиент будет поддерживать соединение, не отправляя данных.
                    int byteRead = clientSocket.getInputStream().read(); // Читаем побайтно, чтобы отслеживать закрытие
                    if (byteRead == -1) { // Клиент закрыл соединение
                        logger.info("Client '{}' closed connection gracefully (EOF).", clientId);
                        break;
                    }
                    // Если клиент отправляет что-то помимо ID (неожиданно)
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
                    clientSocket.close(); // Убеждаемся, что сокет закрыт
                }
            } catch (IOException e) {
                logger.warn("Error closing client socket for {}: {}", clientId, e.getMessage());
            }
            if (clientId != null) {
                connectedClients.remove(clientId); // Удаляем клиента из карты при отключении
                logger.info("TCP client '{}' disconnected. Total active TCP clients: {}", clientId, connectedClients.size());
            }
        }
    }

    /**
     * Отправляет бинарные данные одному или нескольким подключенным TCP-клиентам.
     * @param data Данные для отправки.
     * @param targetClientIDs Список ID клиентов, которым нужно отправить данные.
     */
    public void sendBytes(ByteBuffer data, List<String> targetClientIDs) {
        if (targetClientIDs == null || targetClientIDs.isEmpty()) {
            logger.warn("No target client IDs provided for sending data. Skipping TCP send.");
            return;
        }

        final byte[] bytesToSend = new byte[data.remaining()];
        data.duplicate().get(bytesToSend);

        for (String clientId : targetClientIDs) {
            OutputStream outputStream = connectedClients.get(clientId);
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
                    logger.error("Error sending bytes to TCP client '{}': {}. Removing client.", clientId, e.getMessage(), e); // Стек-трейс
                    connectedClients.remove(clientId);
                } catch (Exception e) {
                    logger.error("Unexpected error during send to TCP client '{}': {}", clientId, e.getMessage(), e); // Стек-трейс
                }
            });
        }
    }

    @PreDestroy
    public void close() {
        System.out.println("DEBUG: TcpServerService @PreDestroy close method called."); // <-- ТРАССИРОВКА
        logger.info("Closing TcpServerService. Shutting down executors and all client connections.");
        running.set(false); // Останавливаем основной цикл accept

        shutdownExecutor(connectionAcceptorExecutor, "Connection Acceptor");
        shutdownExecutor(clientHandlerExecutor, "Client Handler");
        shutdownExecutor(cleanupExecutor, "Cleanup");

        closeServerSocket();

        for (OutputStream os : connectedClients.values()) {
            try {
                os.close();
            } catch (IOException e) {
                logger.warn("Error closing client output stream: {}", e.getMessage());
            }
        }
        connectedClients.clear();
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