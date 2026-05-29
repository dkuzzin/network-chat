package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.protocol.framed.FrameDecoder;
import ru.nsu.ccfit.kuzin.common.protocol.framed.FrameEncoder;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ClientConnection {
    private static final int READ_BUFFER_SIZE = 8192;
    private final SocketChannel channel;           //Socket channel может проверить данные но не ждать
    private final SocketAddress remoteAddress;
    private final Logger logger;
    private final ExecutorService workerPool;
    private final FrameDecoder decoder = new FrameDecoder();
    private final ByteBuffer readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);

    private final Queue<ByteBuffer> outgoingMessages = new ConcurrentLinkedQueue<>();
    private final Queue<Message> incomingMessages = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final ClientCommandHandler commandHandler;
    private volatile long lastSeenMillis;
    private volatile boolean closeAfterWrite = false;

    public ClientConnection(SocketChannel channel, ChatRoom chatRoom,
            ExecutorService workerPool, Logger logger) throws IOException
    {
        this.channel = channel;
        this.remoteAddress = channel.getRemoteAddress();
        this.workerPool = workerPool;
        this.logger = logger;

        this.commandHandler = new ClientCommandHandler(chatRoom, new QueuedProtocolWriter(this), logger);
        this.lastSeenMillis = System.currentTimeMillis();
    }

    public List<Message> readAvailableMessages() throws IOException {
        List<Message> messages = new ArrayList<>();

        while (!closed.get()) {
            readBuffer.clear();

            int bytesRead = channel.read(readBuffer);

            if (bytesRead == -1) throw new EOFException("Client closed connection");
            if (bytesRead == 0) break;

            lastSeenMillis = System.currentTimeMillis();

            readBuffer.flip();
            messages.addAll(decoder.decode(readBuffer));
        }

        return messages;
    }

    public void submitMessage(Message message) {
        if (closed.get()) {
            return;
        }
        logger.info("Submit message to worker pool from "  +
                remoteAddress  + ": " + message.getClass().getSimpleName());

        incomingMessages.add(message);

        if (processing.compareAndSet(false, true)) {
            workerPool.submit(this::processIncomingMessages);
        }
    }

    private void processIncomingMessages() {
        try {
            while (!closed.get()) {
                Message message = incomingMessages.poll();
                if (message == null) return;

                try {
                    logger.info("Worker " + Thread.currentThread().getName() + " handles "
                            + message.getClass().getSimpleName() + " from " + remoteAddress);
                    commandHandler.handle(message);

                    if (!commandHandler.isRunning()) {
                        closeAfterWrite = true;
                        return;
                    }
                } catch (IOException e) {
                    logger.warning("Failed to handle message from "
                            + remoteAddress + ": " + e.getMessage());
                    closeNow();
                    return;
                }
            }
        } finally {
            processing.set(false);

            if (!incomingMessages.isEmpty()
                    && processing.compareAndSet(false, true)) {
                workerPool.submit(this::processIncomingMessages);
            }
        }
    }

    public void enqueue(Message message) throws IOException {
        if (closed.get()) {
            throw new IOException("Connection is already closed");
        }

        outgoingMessages.add(FrameEncoder.encode(message));
    }

    public void writePendingMessages() throws IOException {
        while (!closed.get()) {
            ByteBuffer buffer = outgoingMessages.peek();
            if (buffer == null) {
                if (closeAfterWrite) {
                    closeNow();
                }
                return;
            }

            channel.write(buffer);
            if (buffer.hasRemaining()) {
                return;
            }

            outgoingMessages.poll();
        }
    }

    public boolean isTimedOut(long nowMillis, int timeoutMillis) {
        return timeoutMillis > 0 && nowMillis - lastSeenMillis > timeoutMillis;
    }

    public void closeNow() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        commandHandler.disconnectCurrentSession();

        try {
            channel.close();
        } catch (IOException e) {
            logger.warning("Failed to close client channel "
                    + remoteAddress + ": " + e.getMessage());
        }

        outgoingMessages.clear();
        incomingMessages.clear();

        logger.info("Client connection closed: " + remoteAddress);
    }

    public boolean isClosed() {
        return closed.get();
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}