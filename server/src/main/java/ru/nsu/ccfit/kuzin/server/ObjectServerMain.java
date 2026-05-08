package ru.nsu.ccfit.kuzin.server;

public class ObjectServerMain {
    public static void main(String[] args){
        ServerConfig config = ServerConfig.load(); //TODO обработка исключения

        ChatServer server = new ChatServer(config);
        server.start();
    }
}
