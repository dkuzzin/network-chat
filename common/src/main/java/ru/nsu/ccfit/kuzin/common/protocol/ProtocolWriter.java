package ru.nsu.ccfit.kuzin.common.protocol;

import ru.nsu.ccfit.kuzin.common.message.Message;

import java.io.IOException;

public interface ProtocolWriter {
    void write(Message message) throws IOException;
}
