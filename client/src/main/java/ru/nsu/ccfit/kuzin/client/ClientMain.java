package ru.nsu.ccfit.kuzin.client;

import ru.nsu.ccfit.kuzin.client.ui.ChatFrame;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            ChatFrame frame = new ChatFrame();
            frame.setVisible(true);
        });
    }
}

//TODO пользователь отавливался по таймауту и event history