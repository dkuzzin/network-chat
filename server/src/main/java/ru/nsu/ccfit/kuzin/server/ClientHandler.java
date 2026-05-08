package ru.nsu.ccfit.kuzin.server;

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
            Thread.sleep(5000);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            logger.warning("Client handler interrupted");
        }finally {
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
