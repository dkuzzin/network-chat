package ru.nsu.ccfit.kuzin.common.protocol.framed;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class FramedProtocolWriter implements ProtocolWriter {
    private final OutputStream outputStream;

    public FramedProtocolWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public synchronized void write(Message message) throws IOException {
        ByteBuffer frame = FrameEncoder.encode(message);

        byte[] bytes = new byte[frame.remaining()];
        frame.get(bytes);

        outputStream.write(bytes);
        outputStream.flush();
    }
}