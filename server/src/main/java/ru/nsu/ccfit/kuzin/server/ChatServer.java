package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private static final int POLL_INTERVAL_MS = 10;

    private final ServerConfig config;
    private final ChatRoom chatRoom;

    private final ExecutorService workerPool;
    private final ScheduledExecutorService pollerExecutor;

    private final List<ClientConnection> connections = new CopyOnWriteArrayList<>();

    public ChatServer(ServerConfig config) {
        this.config = config;
        this.chatRoom = new ChatRoom(
                config.getHistorySize(),
                config.getMaxNameSize(),
                config.getMaxMessageSize()
        );

        this.workerPool = Executors.newFixedThreadPool(config.getThreadsCount());
        this.pollerExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        pollerExecutor.scheduleWithFixedDelay(
                this::pollConnections, 0, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(config.getPort()));

            logger.info("\n\nStart server===========================================");
            logger.info("Server started on port " + config.getPort());
            logger.info("Waiting for clients...");

            while (true) {
                SocketChannel clientChannel = serverChannel.accept();
                clientChannel.configureBlocking(false); //При чтении клиента не будет ждать, если данных нет

                ClientConnection connection = new ClientConnection(
                        clientChannel, chatRoom, workerPool, logger);

                connections.add(connection);
                logger.info("Client connected: " + connection.getRemoteAddress());
            }
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void pollConnections() {
        for (ClientConnection connection : connections) {
            if (connection.isClosed()) {
                connections.remove(connection);
                continue;
            }

            try {
                List<Message> messages = connection.readAvailableMessages();

                for (Message message : messages) {
                    logger.info("Received message from "
                            + connection.getRemoteAddress() + ": " + message);

                    connection.submitMessage(message);
                }

                connection.writePendingMessages();

                if (connection.isTimedOut(System.currentTimeMillis(), config.getClientTimeoutMs())) {
                    logger.warning("Client timeout: " + connection.getRemoteAddress());
                    connection.closeNow();
                    connections.remove(connection);
                }

                if (connection.isClosed()) {
                    connections.remove(connection);
                }
            } catch (IOException e) {
                logger.warning("I/O error with client "
                        + connection.getRemoteAddress()
                        + ": "
                        + e.getMessage());

                connection.closeNow();
                connections.remove(connection);
            } catch (RuntimeException e) {
                logger.warning("Unexpected error with client "
                        + connection.getRemoteAddress()
                        + ": "
                        + e.getMessage());

                connection.closeNow();
                connections.remove(connection);
            }
        }
    }

    private void shutdown() {
        for (ClientConnection connection : connections) {
            connection.closeNow();
        }

        pollerExecutor.shutdown();
        workerPool.shutdown();
    }
}