package ru.nsu.ccfit.kuzin.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer {
    private final ServerConfig config;
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());
    private final ExecutorService clientExecutor;
    private final ChatRoom chatRoom;

    public ChatServer(ServerConfig config){
        this.config = config;
        this.clientExecutor = Executors.newFixedThreadPool(config.getThreadsCount());
        this.chatRoom = new ChatRoom();

        if (!config.isLoggingEnabled()){
            logger.setLevel(Level.OFF);
        }
    }

    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(config.getPort())){
            logger.info("Server started on port " + config.getPort());
            logger.info("Waiting for clients...");

            while (true){ //todo Может быть есть более дипломатичный способ остановки сервера чем ctrl+c

                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(config.getClientTimeoutMs());
                logger.info("Client connected: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler  = new ClientHandler(logger, clientSocket, chatRoom);
                clientExecutor.submit(handler);
            }
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        }finally{
            clientExecutor.shutdown();
        }
    }
}
