package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.message.response.*;
import ru.nsu.ccfit.kuzin.common.protocol.*;
import ru.nsu.ccfit.kuzin.common.protocol.object.*;
import ru.nsu.ccfit.kuzin.common.exception.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
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

        ClientCommandHandler commandHandler = null;
        try{
            ProtocolWriter writer = new ObjectProtocolWriter(socket.getOutputStream());
            ProtocolReader reader = new ObjectProtocolReader(socket.getInputStream());

            commandHandler = new ClientCommandHandler(chatRoom, writer, logger);

            while (commandHandler.isRunning()){ //TODO убрать while
                Message message = reader.read();
                logger.info("Received message from " + clientAddress + ": " + message);
                commandHandler.handle(message);
            }

        }catch (SocketTimeoutException e) {
            logger.warning("Client timeout: " + clientAddress);
        }catch (ProtocolException e) {
            logger.warning("Protocol error from " + clientAddress + ": " + e.getMessage());
        } catch (IOException e) {
            logger.warning("I/O error with client " + clientAddress + ": " + e.getMessage());
        } finally {
            if (commandHandler != null){
                commandHandler.disconnectCurrentSession();
            }
            closeSocket();
            logger.info("Client handler finished: " + clientAddress);
        }
    }

    private void closeSocket(){
        try {
            socket.close();
        } catch (IOException e) {
            logger.warning("Failed to close client socket: " + e.getMessage());
        }
    }
}
