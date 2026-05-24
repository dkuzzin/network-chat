package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.message.command.*;
import ru.nsu.ccfit.kuzin.common.message.event.MessageEvent;
import ru.nsu.ccfit.kuzin.common.message.event.UserLoginEvent;
import ru.nsu.ccfit.kuzin.common.message.event.UserLogoutEvent;
import ru.nsu.ccfit.kuzin.common.message.response.*;
import ru.nsu.ccfit.kuzin.common.protocol.*;
import ru.nsu.ccfit.kuzin.common.protocol.object.*;
import ru.nsu.ccfit.kuzin.common.exception.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.logging.Logger;

public class ClientCommandHandler {
    private final ChatRoom chatRoom;
    private final ProtocolWriter writer;
    private final Logger logger;

    private ClientSession session;
    private boolean running = true;

    public ClientCommandHandler(ChatRoom chatRoom, ProtocolWriter writer, Logger logger) {
        this.chatRoom = chatRoom;
        this.writer = writer;
        this.logger = logger;
    }

    public ClientSession getSession() {
        return session;
    }

    public boolean isRunning(){
        return running;
    }

    public void handle(Message message) throws IOException {
        switch (message){
            case LoginCommand command -> handleLogin(command);
            case LogoutCommand command -> handleLogout(command);
            case ListCommand command -> handleList(command);
            case ChatMessageCommand command -> handleChatMessage(command);
            case PingCommand command -> handlePing(command);
            default -> writer.write(new ErrorResponse("Unknown command"));
        }
    }

    private void handleLogin(LoginCommand command) throws IOException {
        if (session != null) {
            writer.write(new ErrorResponse("Client is already logged in"));
            return;
        }

        ChatResult<ClientSession> result = chatRoom.login(
                command.name(),
                command.clientType(),
                writer
        );

        if (result.isSuccess()) {
            session = result.getValue();

            writer.write(new SuccessResponse(session.getSessionId()));

            sendHistoryToClient();

            UserLoginEvent event = new UserLoginEvent(session.getName());
            chatRoom.addToHistory(event);
            chatRoom.broadcastExcept(event, session.getSessionId());

            logger.info("User logged in: " + session.getName());
        } else {
            writer.write(new ErrorResponse(result.getErrorMessage()));

            logger.info("Login failed: " + result.getErrorMessage());
        }
    }

    private void handleLogout(LogoutCommand command) throws IOException {
        if (rejectIfNotLoggedIn()) {
            return;
        }

        if (rejectIfInvalidSession(command.sessionId())) {
            return;
        }

        String name = session.getName();
        disconnectCurrentSession();

        writer.write(new SuccessResponse(null));
        running = false;

        logger.info("User logged out: " + name);
    }

    private void handleList(ListCommand command) throws IOException {
        if (rejectIfNotLoggedIn()) {
            return;
        }

        if (rejectIfInvalidSession(command.sessionId())) {
            return;
        }

        ChatResult<List<UserInfo>> result = chatRoom.getUser(command.sessionId());

        if (result.isSuccess()) {
            writer.write(new UserListResponse(result.getValue()));
        } else {
            writer.write(new ErrorResponse(result.getErrorMessage()));
        }
    }

    private void handleChatMessage(ChatMessageCommand command) throws IOException {
        if (rejectIfNotLoggedIn()) {
            return;
        }

        if (rejectIfInvalidSession(command.sessionId())) {
            return;
        }

        ChatResult<MessageEvent> result = chatRoom.createMessageEvent(
                command.sessionId(),
                command.text()
        );

        if (!result.isSuccess()) {
            writer.write(new ErrorResponse(result.getErrorMessage()));
            return;
        }

        writer.write(new SuccessResponse(null));

        chatRoom.broadcast(result.getValue());

        logger.info("Message from " + session.getName() + ": " + command.text());
    }
    public void disconnectCurrentSession() {
        if (session == null) {
            return;
        }

        ClientSession oldSession = session;

        boolean disconnected = chatRoom.disconnect(oldSession);
        session = null;

        if (disconnected) {
            UserLogoutEvent event = new UserLogoutEvent(oldSession.getName());

            chatRoom.addToHistory(event);
            chatRoom.broadcastExcept(event, oldSession.getSessionId());
        }
    }

    private boolean rejectIfNotLoggedIn() throws IOException {
        if (session == null) {
            writer.write(new ErrorResponse("Client is not logged in"));
            return true;
        }

        return false;
    }

    private boolean rejectIfInvalidSession(String sessionId) throws IOException {
        if (!session.getSessionId().equals(sessionId)) {
            writer.write(new ErrorResponse("Invalid session"));
            return true;
        }

        return false;
    }

    private void sendHistoryToClient() throws IOException{
        for (Message event: chatRoom.getMessageHistory()){
            writer.write(event);
        }

        writer.write(new MessageEvent("", "[* You have joined to the chat ]"));
    }

    private void handlePing(PingCommand command) throws IOException {
        if (session == null) {
            writer.write(new ErrorResponse("Client is not logged in"));
            return;
        }

        if (!session.getSessionId().equals(command.sessionId())) {
            writer.write(new ErrorResponse("Invalid session ID"));
            return;
        }
    }
}
