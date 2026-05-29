package ru.nsu.ccfit.kuzin.common.protocol.framed;

import ru.nsu.ccfit.kuzin.common.message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class FrameEncoder {
    private FrameEncoder() {}

    public static ByteBuffer encode(Message message) throws IOException {
        byte[] payload = MessageCodec.encode(message);

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + payload.length);

        buffer.putInt(payload.length);
        buffer.put(payload);
        buffer.flip();

        return buffer;
    }
}