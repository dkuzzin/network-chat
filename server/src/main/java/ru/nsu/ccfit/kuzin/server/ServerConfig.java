package ru.nsu.ccfit.kuzin.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {
    private final int port;
    private final boolean loggingEnabled;
    private final int historySize;
    private final int clientTimeoutMs;

    public ServerConfig(int port, boolean loggingEnabled, int historySize, int clientTimeoutMs){
        this.port = port;
        this.loggingEnabled = loggingEnabled;
        this.historySize = historySize;
        this.clientTimeoutMs = clientTimeoutMs;
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

    public static ServerConfig load(String resourceName){
        Properties properties = new Properties();
        try(InputStream inputStream = ServerConfig.class
                .getClassLoader()
                .getResourceAsStream(resourceName)){
            if (inputStream != null){
                properties.load(inputStream);
            }
        }catch (IOException e){
            throw new IllegalStateException("Failed to load server config: " + resourceName, e);//todo свое или
        }

        int port = Integer.parseInt(properties.getProperty("port", "8080"));
        boolean loggingEnabled = Boolean.parseBoolean(properties.getProperty("logging.enabled", "true"));
        int historySize = Integer.parseInt(properties.getProperty("history.size", "20"));
        int clientTimeoutMs = Integer.parseInt(properties.getProperty("client.timeout.ms", "120000"));

        return new ServerConfig(port, loggingEnabled, historySize, clientTimeoutMs);
    }

}
