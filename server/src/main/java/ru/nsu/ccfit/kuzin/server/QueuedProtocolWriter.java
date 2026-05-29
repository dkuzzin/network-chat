package ru.nsu.ccfit.kuzin.server;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolWriter;

import java.io.IOException;

public class QueuedProtocolWriter implements ProtocolWriter {
    private final ClientConnection connection;

    public QueuedProtocolWriter(ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void write(Message message) throws IOException {
        connection.enqueue(message);
    }
}