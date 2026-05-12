package ru.nsu.ccfit.kuzin.client.ui;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            ChatFrame frame = new ChatFrame();
            frame.setVisible(true);
        });
    }
}
