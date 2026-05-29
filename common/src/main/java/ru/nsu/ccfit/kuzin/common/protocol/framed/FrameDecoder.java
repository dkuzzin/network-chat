package ru.nsu.ccfit.kuzin.common.protocol.framed;

import ru.nsu.ccfit.kuzin.common.exception.ProtocolException;
import ru.nsu.ccfit.kuzin.common.message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FrameDecoder {
    private static final int MAX_PAYLOAD_SIZE = 10 * 1024 * 1024;

    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
    private ByteBuffer payloadBuffer;

    public List<Message> decode(ByteBuffer inputBuffer) throws IOException {
        List<Message> messages = new ArrayList<>();

        while (inputBuffer.hasRemaining()) {
            if (payloadBuffer == null) {
                copyBytes(inputBuffer, lengthBuffer);
                if (lengthBuffer.hasRemaining()) {
                    break;
                }

                lengthBuffer.flip();
                int payloadSize = lengthBuffer.getInt();
                lengthBuffer.clear();

                if (payloadSize <= 0) {
                    throw new ProtocolException("Invalid message size: " + payloadSize);
                }

                if (payloadSize > MAX_PAYLOAD_SIZE) {
                    throw new ProtocolException("Message is too large: " + payloadSize);
                }

                payloadBuffer = ByteBuffer.allocate(payloadSize);
            }

            copyBytes(inputBuffer, payloadBuffer);

            if (payloadBuffer.hasRemaining()) {
                break;
            }

            payloadBuffer.flip();

            byte[] payload = new byte[payloadBuffer.remaining()];
            payloadBuffer.get(payload);

            payloadBuffer = null;

            Message message = MessageCodec.decode(payload);
            messages.add(message);
        }

        return messages;
    }

    private void copyBytes(ByteBuffer source, ByteBuffer target) {
        int bytesToCopy = Math.min(source.remaining(), target.remaining());

        int oldLimit = source.limit();

        source.limit(source.position() + bytesToCopy);
        target.put(source);

        source.limit(oldLimit);
    }
}