package ru.nsu.ccfit.kuzin.server;

import java.io.IOException;
import java.net.Socket;

import java.util.logging.Logger;

public class ClientHandler implements Runnable{
    private final Logger logger;
    private final Socket socket;

    public ClientHandler(Logger logger, Socket socket){
        this.socket = socket;
        this.logger = logger;
    }

    @Override
    public void run() {
        logger.info("Client handler started: " + socket.getRemoteSocketAddress());
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
        } catch (IOException e) {
            logger.warning("Failed to close client socket: " + e.getMessage());
        }
    }
}
