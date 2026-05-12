package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.server.logging.LogConfigurator;

import java.io.IOException;
import java.nio.file.Path;

public class ObjectServerMain {
    public static void main(String[] args) {
        ServerConfig config;

        try {
            config = ServerConfig.load();
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid server config: " + e.getMessage());
            return;
        }

        try {
            LogConfigurator.configure(Path.of("logs/server.log"), config.isLoggingEnabled());
        } catch (IOException e) {
            System.err.println("Failed to configure logging: " + e.getMessage());
            return;
        }

        ChatServer server = new ChatServer(config);
        server.start();
    }
}
