package ru.nsu.ccfit.kuzin.protocol.object;

import ru.nsu.ccfit.kuzin.message.Message;
import ru.nsu.ccfit.kuzin.protocol.ProtocolWriter;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ObjectProtocolWriter implements ProtocolWriter {
    private final ObjectOutputStream out;

    public ObjectProtocolWriter(OutputStream out) throws IOException{
        this.out = new ObjectOutputStream(out);
        this.out.flush();
    }

    @Override
    public synchronized void write(Message message) throws IOException{
        out.writeObject(message);
        out.flush();
    }
}
