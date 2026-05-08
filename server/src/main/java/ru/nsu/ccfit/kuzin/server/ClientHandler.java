package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.message.command.LoginCommand;
import ru.nsu.ccfit.kuzin.common.message.response.ErrorResponse;
import ru.nsu.ccfit.kuzin.common.message.response.SuccessResponse;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolReader;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolWriter;
import ru.nsu.ccfit.kuzin.common.protocol.object.ObjectProtocolReader;
import ru.nsu.ccfit.kuzin.common.protocol.object.ObjectProtocolWriter;

import java.io.IOException;
import java.net.Socket;

import java.net.SocketAddress;
import java.util.logging.Logger;

public class ClientHandler implements Runnable{
    private final Logger logger;
    private final Socket socket;
    private SocketAddress clientAddress;
    private final ChatRoom chatRoom;

    public ClientHandler(Logger logger, Socket socket, ChatRoom chatRoom){
        this.socket = socket;
        this.logger = logger;
        this.chatRoom = chatRoom;
    }

    @Override
    public void run() {
        clientAddress = socket.getRemoteSocketAddress();
        logger.info("Client handler started: " + clientAddress);
        try{
            ProtocolWriter writer = new ObjectProtocolWriter(socket.getOutputStream());
            ProtocolReader reader = new ObjectProtocolReader(socket.getInputStream());

            Message message = reader.read();
            handleMessage(message, writer);

            logger.info("Received message from " + clientAddress + ": " + message);

        } catch (IOException e) {
            throw new RuntimeException(e);//todo
        } finally {
            closeSocket();
        }
    }

    private void handleMessage(Message message, ProtocolWriter writer) throws IOException{
        if (message instanceof LoginCommand){
            handleLogin((LoginCommand) message, writer);
        }else{
            writer.write(new ErrorResponse("Unknown message"));//todo
        }
    }

    private void handleLogin(LoginCommand command, ProtocolWriter writer) throws IOException{
        ChatResult<ClientSession> result = chatRoom.login(command.name(), command.clientType(), writer);
        if (result.isSuccess()){
            ClientSession session = result.getValue();
            writer.write(new SuccessResponse(session.getSessionId()));
            logger.info("User logged in: " + session.getName());
        }else{
            writer.write(new ErrorResponse(result.getErrorMessage()));
            logger.info("Login failed: " + result.getErrorMessage());
        }
    }

    private void closeSocket(){
        try {
            socket.close();
            logger.info("Client handler finished: " + clientAddress);
        } catch (IOException e) {
            logger.warning("Failed to close client socket: " + e.getMessage());
        }
    }
}
