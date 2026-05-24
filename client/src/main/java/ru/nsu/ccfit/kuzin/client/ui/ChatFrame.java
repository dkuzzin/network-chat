package ru.nsu.ccfit.kuzin.client.ui;

import ru.nsu.ccfit.kuzin.client.core.ChatClientListener;
import ru.nsu.ccfit.kuzin.client.core.ObjectChatClient;
import ru.nsu.ccfit.kuzin.common.message.response.UserInfo;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;


public class ChatFrame extends JFrame implements
        LoginPanel.LoginActionListener,
        ChatPanel.ChatActionListener,
        ChatClientListener {

    private static final String CLIENT_TYPE = "SwingUI Client";

    private static final String LOGIN_CARD = "login";
    private static final String CHAT_CARD = "chat";
    private boolean disconnectHandled = false;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(cardLayout);

    private final LoginPanel loginPanel = new LoginPanel(this);
    private final ChatPanel chatPanel = new ChatPanel(this);

    private String currentHost;
    private int currentPort;
    private String currentName;
    private ObjectChatClient client;

    public ChatFrame() {
        super("<./talk.exe>");

        configureWindow();
        createLayout();
    }

    private void configureWindow() {
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }

    private void handleWindowClosing() {
        closeClient();
        dispose();
    }

    private void closeClient() {
        if (client == null) {
            return;
        }

        try {
            if (client.isConnected() && client.isLoggedIn()) {
                client.logout();
            }
        } catch (IOException | IllegalStateException _) {
        } finally {
            client.close();
            client = null;
        }
    }

    private void createLayout() {
        rootPanel.setBackground(java.awt.Color.BLACK);
        rootPanel.add(loginPanel, LOGIN_CARD);
        rootPanel.add(chatPanel, CHAT_CARD);

        add(rootPanel);

        cardLayout.show(rootPanel, LOGIN_CARD);
    }

    @Override
    public void onLoginRequested(String host, int port, String name) {
        loginPanel.setLoginEnabled(false);
        currentHost = host;
        currentPort = port;
        currentName = name;
        try {
            client = new ObjectChatClient(host, port, CLIENT_TYPE, this);

            client.connect();
            client.login(name);
        } catch (IOException e) {
            closeClient();
            loginPanel.setLoginEnabled(true);
            showError("Connection error: " + e.getMessage());
        } catch (IllegalStateException e) {
            closeClient();
            loginPanel.setLoginEnabled(true);
            showError("Client error: " + e.getMessage());
        }
    }

    @Override
    public void onLoginError(String message) {
        showError(message);
    }

    @Override
    public void onSendRequested(String text) {
        try {
            client.sendMessage(text);
        } catch (IOException e) {
            showError("Send error: " + e.getMessage());
        } catch (IllegalStateException e) {
            showError("Client error: " + e.getMessage());
        }
    }


    @Override
    public void onLogoutRequested() {
        switchToLogin();
    }

    @Override
    public void onChatError(String message) {
        showError(message);
    }

    @Override
    public void onLoginSuccess(String sessionId) {
        SwingUtilities.invokeLater(() -> {
            disconnectHandled = false;
            chatPanel.clear();
            chatPanel.setConnectionInfo(currentName, currentHost, currentPort);
            //chatPanel.appendSystemMessage("You have joined the chat.");
            cardLayout.show(rootPanel, CHAT_CARD);

            requestUserListAfterLogin();
        });
    }

    private void handleUnexpectedDisconnect(String message) {
        if (disconnectHandled) {
            return;
        }

        disconnectHandled = true;

        JOptionPane.showMessageDialog(
                this,
                message,
                "Disconnected",
                JOptionPane.WARNING_MESSAGE
        );

        switchToLogin();
    }

    @Override
    public void onError(String message) {
        SwingUtilities.invokeLater(() -> {
            showError("Error: " + message);

            if (client != null && !client.isLoggedIn()) {
                closeClient();
                loginPanel.setLoginEnabled(true);
                cardLayout.show(rootPanel, LOGIN_CARD);
            } else {
                chatPanel.appendSystemMessage("Error: " + message);
            }
        });
    }

    @Override
    public void onUserList(List<UserInfo> users) {
        SwingUtilities.invokeLater(() -> chatPanel.setUsers(users));
    }

    @Override
    public void onMessageReceived(String from, String text) {
        SwingUtilities.invokeLater(() -> chatPanel.appendMessage(from, text));
    }

    @Override
    public void onUserLogin(String name) {
        SwingUtilities.invokeLater(() -> {
            chatPanel.appendSystemMessage(name + " joined");
            requestUserListAfterLogin();
        });
    }

    @Override
    public void onUserLogout(String name) {
        SwingUtilities.invokeLater(() -> {
            chatPanel.appendSystemMessage(name + " left");
            requestUserListAfterLogin();
        });
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() ->
                handleUnexpectedDisconnect("You were disconnected from the server.")
        );
    }

    @Override
    public void onConnectionError(String message) {
        SwingUtilities.invokeLater(() ->
                handleUnexpectedDisconnect("Connection lost: " + message)
        );
    }

    private void requestUserListAfterLogin() {
        if (client == null || !client.isConnected() || !client.isLoggedIn()) {
            return;
        }

        try {
            client.requestUserList();
        } catch (IOException | IllegalStateException e) {
            chatPanel.appendSystemMessage("Failed to request user list: " + e.getMessage());
        }
    }

    private void switchToLogin() {
        closeClient();

        loginPanel.setLoginEnabled(true);
        cardLayout.show(rootPanel, LOGIN_CARD);
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        this,
                        message,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                )
        );
    }
}