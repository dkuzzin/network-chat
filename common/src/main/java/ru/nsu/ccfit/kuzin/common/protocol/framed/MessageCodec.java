package ru.nsu.ccfit.kuzin.common.protocol.framed;

import ru.nsu.ccfit.kuzin.common.exception.ProtocolException;
import ru.nsu.ccfit.kuzin.common.message.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class MessageCodec {
    private MessageCodec() {}

    public static byte[] encode(Message message) throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("Message is null");
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(message);
            objectStream.flush();
        }

        return byteStream.toByteArray();
    }

    public static Message decode(byte[] bytes) throws IOException, ProtocolException {
        if (bytes == null) {
            throw new ProtocolException("Message bytes array is null");
        }

        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);

        try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            Object object = objectStream.readObject();

            if (!(object instanceof Message message)) {
                throw new ProtocolException("Decoded object is not a chat message");
            }

            return message;
        } catch (ClassNotFoundException e) {
            throw new ProtocolException("Unknown message class", e);
        }
    }
}