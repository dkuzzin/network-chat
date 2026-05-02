package ru.nsu.ccfit.kuzin.common.protocol.object;

import ru.nsu.ccfit.kuzin.common.exception.ProtocolException;
import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolReader;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;


public class ObjectProtocolReader implements ProtocolReader {
    private final ObjectInputStream in;

    public ObjectProtocolReader(InputStream inputStream) throws IOException {
        this.in = new ObjectInputStream(inputStream);
    }

    @Override
    public Message read() throws IOException, ProtocolException{
        try{
            Object object = in.readObject();

            if (!(object instanceof Message message)){
                throw new ProtocolException("Received object is not a chat message");
            }

            return message;
        } catch (ClassNotFoundException e) {
            throw new ProtocolException("Unknown message class", e);
        }
    }
}
