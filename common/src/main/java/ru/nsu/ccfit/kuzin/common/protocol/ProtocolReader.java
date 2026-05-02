package ru.nsu.ccfit.kuzin.common.protocol;

import ru.nsu.ccfit.kuzin.common.exception.ProtocolException;
import ru.nsu.ccfit.kuzin.common.message.Message;

import java.io.IOException;

public interface ProtocolReader {
    Message read() throws IOException, ProtocolException;
}
