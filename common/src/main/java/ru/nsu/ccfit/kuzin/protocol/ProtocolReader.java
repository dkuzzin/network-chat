package ru.nsu.ccfit.kuzin.protocol;

import ru.nsu.ccfit.kuzin.exception.ProtocolException;
import ru.nsu.ccfit.kuzin.message.Message;

import java.io.IOException;

public interface ProtocolReader {
    Message read() throws IOException, ProtocolException;
}
