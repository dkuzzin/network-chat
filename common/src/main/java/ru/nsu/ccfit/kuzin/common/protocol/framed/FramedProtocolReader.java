package ru.nsu.ccfit.kuzin.common.protocol.framed;

import ru.nsu.ccfit.kuzin.common.exception.ProtocolException;
import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolReader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FramedProtocolReader implements ProtocolReader {
    private static final int MAX_PAYLOAD_SIZE = 10 * 1024 * 1024;

    private final DataInputStream inputStream;

    public FramedProtocolReader(InputStream inputStream) {
        this.inputStream = new DataInputStream(inputStream);
    }

    @Override
    public Message read() throws IOException, ProtocolException {
        int payloadSize = inputStream.readInt();

        if (payloadSize <= 0) {
            throw new ProtocolException("Invalid message size: " + payloadSize);
        }

        if (payloadSize > MAX_PAYLOAD_SIZE) {
            throw new ProtocolException("Message is too large: " + payloadSize);
        }

        byte[] payload = new byte[payloadSize];
        inputStream.readFully(payload);

        return MessageCodec.decode(payload);
    }
}