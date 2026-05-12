package ru.nsu.ccfit.kuzin.client.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

public class LoginPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = new Color(0, 255, 80);

    private static final Font TITLE_FONT = new Font(Font.MONOSPACED, Font.BOLD, 48);
    private static final Font TEXT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font(Font.MONOSPACED, Font.BOLD, 16);

    private final JTextField hostField = new JTextField("localhost");
    private final JTextField portField = new JTextField("8080");
    private final JTextField nameField = new JTextField();

    private final JButton loginButton = new JButton("Login");

    private final LoginActionListener listener;

    public LoginPanel(LoginActionListener listener) {
        this.listener = listener;

        createLayout();

        hostField.setEditable(false);
        portField.setEditable(false);
        addListeners();
    }


    public void setLoginEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
    }

    private void createLayout() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("<./talk.exe>", JLabel.CENTER);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(TITLE_FONT);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        JPanel formPanel = createFormPanel();

        add(titlePanel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setBackground(BACKGROUND_COLOR);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

        wrapper.add(createInputRow("Host:", hostField));
        wrapper.add(Box.createVerticalStrut(8));

        wrapper.add(createInputRow("Port:", portField));
        wrapper.add(Box.createVerticalStrut(8));

        wrapper.add(createInputRow("Name:", nameField));
        wrapper.add(Box.createVerticalStrut(16));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonRow.setBackground(BACKGROUND_COLOR);

        configureButton(loginButton);
        buttonRow.add(loginButton);

        wrapper.add(buttonRow);

        return wrapper;
    }

    private JPanel createInputRow(String labelText, JTextField field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row.setBackground(BACKGROUND_COLOR);

        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT_COLOR);
        label.setFont(TEXT_FONT);

        configureTextField(field);

        row.add(label);
        row.add(field);

        return row;
    }

    private void configureButton(JButton button) {
        button.setForeground(TEXT_COLOR);
        button.setFont(BUTTON_FONT);

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        button.setBorder(BorderFactory.createLineBorder(TEXT_COLOR));
        button.setPreferredSize(new Dimension(120, 32));
    }
    private void configureTextField(JTextField field) {
        field.setPreferredSize(new Dimension(220, 28));
        field.setBackground(BACKGROUND_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setFont(TEXT_FONT);
        field.setBorder(BorderFactory.createLineBorder(TEXT_COLOR));
    }

    private void addListeners() {
        loginButton.addActionListener(event -> handleLoginButton());
    }

    private void handleLoginButton() {
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();
        String name = nameField.getText().trim();

        if (host.isEmpty() || portText.isEmpty() || name.isEmpty()) {
            listener.onLoginError("Host, port and name must not be empty");
            return;
        }

        int port;

        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            listener.onLoginError("Port must be a number");
            return;
        }

        listener.onLoginRequested(host, port, name);
    }

    public interface LoginActionListener {
        void onLoginRequested(String host, int port, String name);

        void onLoginError(String message);
    }
}