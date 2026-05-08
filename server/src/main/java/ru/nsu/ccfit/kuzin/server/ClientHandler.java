package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;
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

    public ClientHandler(Logger logger, Socket socket){
        this.socket = socket;
        this.logger = logger;
    }

    @Override
    public void run() {
        clientAddress = socket.getRemoteSocketAddress();
        logger.info("Client handler started: " + clientAddress);
        try{
            ProtocolWriter writer = new ObjectProtocolWriter(socket.getOutputStream());
            ProtocolReader reader = new ObjectProtocolReader(socket.getInputStream());

            Message message = reader.read();

            logger.info("Received message from " + clientAddress + ": " + message);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeSocket();
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
