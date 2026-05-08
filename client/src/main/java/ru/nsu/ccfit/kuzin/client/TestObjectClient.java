package ru.nsu.ccfit.kuzin.client;

import ru.nsu.ccfit.kuzin.common.message.Message;
import ru.nsu.ccfit.kuzin.common.message.command.LoginCommand;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolReader;
import ru.nsu.ccfit.kuzin.common.protocol.ProtocolWriter;
import ru.nsu.ccfit.kuzin.common.protocol.object.ObjectProtocolReader;
import ru.nsu.ccfit.kuzin.common.protocol.object.ObjectProtocolWriter;

import java.io.IOException;
import java.net.Socket;

public class TestObjectClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080)) {
            ProtocolWriter writer = new ObjectProtocolWriter(socket.getOutputStream());
            ProtocolReader reader = new ObjectProtocolReader(socket.getInputStream());

            writer.write(new LoginCommand("Danil", "Test Object Client"));
            Message response = reader.read();

            System.out.println("Server response: " + response);

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}