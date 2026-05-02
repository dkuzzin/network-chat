package ru.nsu.ccfit.kuzin.protocol;

import ru.nsu.ccfit.kuzin.message.Message;

import java.io.IOException;

public interface ProtocolWriter {
    void write(Message message) throws IOException;
}
