package ru.nsu.ccfit.kuzin.client.core;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.message.command.ChatMessageCommand;
import ru.nsu.ccfit.kuzin.common.message.command.ListCommand;
import ru.nsu.ccfit.kuzin.common.message.command.LoginCommand;
import ru.nsu.ccfit.kuzin.common.message.command.LogoutCommand;
import ru.nsu.ccfit.kuzin.common.message.event.MessageEvent;
import ru.nsu.ccfit.kuzin.common.message.event.UserLoginEvent;
import ru.nsu.ccfit.kuzin.common.message.event.UserLogoutEvent;
import ru.nsu.ccfit.kuzin.common.message.response.ErrorResponse;
import ru.nsu.ccfit.kuzin.common.message.response.SuccessResponse;
import ru.nsu.ccfit.kuzin.common.message.response.UserListResponse;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolReader;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolWriter;
import ru.nsu.ccfit.kuzin.common.protocol.object.ObjectProtocolReader;
import ru.nsu.ccfit.kuzin.common.protocol.object.ObjectProtocolWriter;

import java.io.EOFException;
import java.io.IOException;

import ru.nsu.ccfit.kuzin.common.exception.ProtocolException;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.InetSocketAddress;

public class ObjectChatClient {
    private final String host;
    private final int port;
    private final String clientType;
    private final ChatClientListener listener;
    private Thread readerThread;
    private String sessionId;
    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int SOCKET_TIMEOUT_MS = 8000;
    private Socket socket;
    private ProtocolWriter writer;
    private ProtocolReader reader;

    //Так как другой поток может проверять, а один ставить
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public ObjectChatClient(String host, int port, String clientType, ChatClientListener listener) {
        this.host = host;
        this.port = port;
        this.clientType = clientType;
        this.listener = listener;
    }

    public void connect() throws IOException {
        if (connected.get()) {
            throw new IllegalStateException("Client is already connected");
        }

        Socket newSocket = new Socket();

        try {
            newSocket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);

            socket = newSocket;
            writer = new ObjectProtocolWriter(socket.getOutputStream());
            reader = new ObjectProtocolReader(socket.getInputStream());
            connected.set(true);

            startReaderThread();
        } catch (IOException e) {
            try {
                newSocket.close();
            } catch (IOException ignored) {
            }

            throw e;
        }
    }
    public void close() {
        connected.set(false);
        sessionId = null;
        tryCloseSocket();
    }

    public void requestUserList() throws IOException {
        ensureLoggedIn();

        writer.write(new ListCommand(sessionId));
    }


    public boolean isConnected() {
        return connected.get();
    }

    private void ensureConnected() {
        if (!connected.get() || socket == null || socket.isClosed()) {
            throw new IllegalStateException("Client is not connected");
        }
    }

    private void ensureLoggedIn() {
        ensureConnected();

        if (sessionId == null) {
            throw new IllegalStateException("Client is not logged in");
        }
    }

    public boolean isLoggedIn() {
        return sessionId != null;
    }

    private void startReaderThread() {
        readerThread = new Thread(this::readLoop, "serverReader-thread");
        readerThread.start();
    }

    private void readLoop() {
        try {
            while (connected.get()) {
                Message message = reader.read();
                handleServerMessage(message);
            }
        } catch (EOFException e) {
            listener.onDisconnected();
        } catch (ProtocolException e) {
            listener.onConnectionError("Connection error: " + e.getMessage());
        } catch (IOException e) {
            if (connected.get()) {
                listener.onConnectionError("Connection error: " + e.getMessage());
            }
        } finally {
            connected.set(false);
            tryCloseSocket();
        }
    }

    private void tryCloseSocket() {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public void login(String name) throws IOException {
        ensureConnected();
        writer.write(new LoginCommand(name, clientType));
    }

    public void logout() throws IOException {
        ensureLoggedIn();

        writer.write(new LogoutCommand(sessionId));

        sessionId = null;
    }

    public void sendMessage(String text) throws IOException {
        ensureLoggedIn();

        writer.write(new ChatMessageCommand(sessionId, text));
    }

    private void handleServerMessage(Message message) {
        switch (message) {
            case SuccessResponse response -> handleSuccessResponse(response);
            case ErrorResponse response -> handleErrorResponse(response.message());
            case UserListResponse response -> listener.onUserList(response.users());
            case MessageEvent event -> listener.onMessageReceived(event.from(), event.text());
            case UserLoginEvent event -> listener.onUserLogin(event.name());
            case UserLogoutEvent event -> listener.onUserLogout(event.name());
            default -> System.out.println("Received from server: " + message);
        }
    }

    private void handleSuccessResponse(SuccessResponse response) {
        if (response.sessionId() != null) {
            sessionId = response.sessionId();
            listener.onLoginSuccess(sessionId);
        }
    }

    private void handleErrorResponse(String message) {
        listener.onError(message);
    }

}
