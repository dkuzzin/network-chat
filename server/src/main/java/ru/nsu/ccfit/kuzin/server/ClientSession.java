package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolWriter;

import java.io.IOException;

public class ClientSession {
    private final String sessionId;
    private final String name;
    private final String clientType;
    private final ProtocolWriter writer;

    public ClientSession(String sessionId, String name, String clientType, ProtocolWriter writer) {
        this.sessionId = sessionId;
        this.name = name;
        this.clientType = clientType;
        this.writer = writer;
    }

    public String getSessionId() {
        return sessionId;
    }

    public ProtocolWriter getWriter() {
        return writer;
    }

    public String getName() {
        return name;
    }

    public String getClientType() {
        return clientType;
    }

    public void sendToClient(Message message) throws IOException{
        writer.write(message);
    }
}
