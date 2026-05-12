package ru.nsu.ccfit.kuzin.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {
    private final int port;
    private final boolean loggingEnabled;
    private final int historySize;
    private final int clientTimeoutMs;
    private final int threadCount;
    private final int maxNameSize;
    private final int maxMessageSize;
    private static final String CONFIG_PATH = "server.properties";

    public ServerConfig(int port, boolean loggingEnabled,
                        int historySize, int clientTimeoutMs, int threadCount, int maxNameSize, int maxMessageSize){
        this.port = port;
        this.loggingEnabled = loggingEnabled;
        this.historySize = historySize;
        this.clientTimeoutMs = clientTimeoutMs;
        this.threadCount = threadCount;
        this.maxNameSize = maxNameSize;
        this.maxMessageSize = maxMessageSize;
    }

    public static ServerConfig load(){
        Properties properties = new Properties();
        try(InputStream inputStream = ServerConfig.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_PATH)){
            if (inputStream != null){
                properties.load(inputStream);
            }
        }catch (IOException e){
            throw new IllegalStateException("Failed to load server config: " + CONFIG_PATH, e);
        }

        int port = Integer.parseInt(properties.getProperty("port", "8080"));
        boolean loggingEnabled = Boolean.parseBoolean(properties.getProperty("logging.enabled", "true"));
        int historySize = Integer.parseInt(properties.getProperty("history.size", "20"));
        int clientTimeoutMs = Integer.parseInt(properties.getProperty("client.timeout.ms", "120000"));
        int threadCount = Integer.parseInt(properties.getProperty("client.thread.count", "100"));
        int maxNameSize = Integer.parseInt(properties.getProperty("name.maxSize", "20"));
        int maxMessageSize = Integer.parseInt(properties.getProperty("message.maxSize", "1000"));
        if (maxNameSize <= 0) {
            throw new IllegalArgumentException("max.name.length must be positive");
        }

        if (maxMessageSize <= 0) {
            throw new IllegalArgumentException("max.message.length must be positive");
        }
        return new ServerConfig(port, loggingEnabled, historySize, clientTimeoutMs, threadCount, maxNameSize, maxMessageSize);
    }

    public int getPort(){
        return port;
    }

    public boolean isLoggingEnabled(){
        return loggingEnabled;
    }

    public int getHistorySize(){
        return historySize;
    }

    public int getClientTimeoutMs(){
        return clientTimeoutMs;
    }

    public int getThreadsCount(){return threadCount;}

    public int getMaxNameSize() {
        return maxNameSize;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }
}
