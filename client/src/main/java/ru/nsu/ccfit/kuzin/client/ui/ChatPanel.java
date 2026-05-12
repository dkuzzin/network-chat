package ru.nsu.ccfit.kuzin.client.ui;

import ru.nsu.ccfit.kuzin.common.message.response.UserInfo;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.List;

public class ChatPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = new Color(0, 255, 80);

    private static final Font TEXT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font(Font.MONOSPACED, Font.BOLD, 16);

    private final JTextArea chatArea = new JTextArea();
    private final JTextField messageField = new JTextField();

    private final JButton sendButton = new JButton("Send");
    private final JButton logoutButton = new JButton("Logout");

    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JList<String> userList = new JList<>(userListModel);
    private final JLabel headerLabel = new JLabel();
    private final ChatActionListener listener;

    public ChatPanel(ChatActionListener listener) {
        this.listener = listener;

        createLayout();
        addListeners();
    }

    private void createLayout() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        configureChatArea();
        configureUserList();
        configureHeaderLabel();

        add(headerLabel, BorderLayout.NORTH);
        add(createScrollPane(chatArea), BorderLayout.CENTER);
        add(createUserPanel(), BorderLayout.EAST);
        add(createInputPanel(), BorderLayout.SOUTH);
    }

    private void configureHeaderLabel() {
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setBackground(BACKGROUND_COLOR);
        headerLabel.setOpaque(true);
        headerLabel.setFont(TEXT_FONT);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }
    public void setConnectionInfo(String name, String host, int port) {
        headerLabel.setText("Your name: " + name + " | server: " + host + ":" + port);
    }

    private JScrollPane createScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);

        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        scrollPane.setBorder(BorderFactory.createLineBorder(TEXT_COLOR));

        configureScrollBar(scrollPane.getVerticalScrollBar());
        configureScrollBar(scrollPane.getHorizontalScrollBar());

        return scrollPane;
    }

    private void configureScrollBar(JScrollBar scrollBar) {
        scrollBar.setBackground(BACKGROUND_COLOR);
        scrollBar.setForeground(TEXT_COLOR);
        scrollBar.setUnitIncrement(16);

        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = TEXT_COLOR;
                trackColor = BACKGROUND_COLOR;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                g.setColor(TEXT_COLOR);
                g.fillRect(
                        thumbBounds.x,
                        thumbBounds.y,
                        thumbBounds.width,
                        thumbBounds.height
                );
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(BACKGROUND_COLOR);
                g.fillRect(
                        trackBounds.x,
                        trackBounds.y,
                        trackBounds.width,
                        trackBounds.height
                );
            }
        });
    }
    private JButton createZeroButton() {
        JButton button = new JButton();

        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));

        return button;
    }

    private JScrollPane createUserPanel() {
        JScrollPane scrollPane = createScrollPane(userList);
        scrollPane.setPreferredSize(new Dimension(180, 0));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    public void clear() {
        chatArea.setText("");
        userListModel.clear();
        messageField.setText("");
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        configureTextField(messageField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        configureButton(sendButton);
        configureButton(logoutButton);

        buttonPanel.add(sendButton);
        buttonPanel.add(logoutButton);

        panel.add(messageField, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void configureChatArea() {
        chatArea.setEditable(false);
        chatArea.setBackground(BACKGROUND_COLOR);
        chatArea.setForeground(TEXT_COLOR);
        chatArea.setCaretColor(TEXT_COLOR);
        chatArea.setFont(TEXT_FONT);
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
    }

    private void configureUserList() {
        userList.setBackground(BACKGROUND_COLOR);
        userList.setForeground(TEXT_COLOR);
        userList.setFont(TEXT_FONT);
        userList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void configureTextField(JTextField field) {
        field.setBackground(BACKGROUND_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setFont(TEXT_FONT);
        field.setBorder(BorderFactory.createLineBorder(TEXT_COLOR));
    }

    private void configureButton(JButton button) {
        button.setForeground(TEXT_COLOR);
        button.setFont(BUTTON_FONT);

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        button.setBorder(BorderFactory.createLineBorder(TEXT_COLOR));
        button.setPreferredSize(new Dimension(90, 32));
    }

    private void addListeners() {
        sendButton.addActionListener(event -> handleSendButton());
        logoutButton.addActionListener(event -> listener.onLogoutRequested());

        messageField.addActionListener(event -> handleSendButton());
    }

    private void handleSendButton() {
        String text = messageField.getText().trim();

        if (text.isEmpty()) {
            listener.onChatError("Message cannot be empty");
            return;
        }

        listener.onSendRequested(text);
        messageField.setText("");
    }

    public void appendMessage(String from, String text) {
        chatArea.append(from + ": " + text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void appendSystemMessage(String text) {
        chatArea.append("[* " + text + "]\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void setUsers(List<UserInfo> users) {

        userListModel.clear();
        userListModel.addElement(users.size() + " members");
        for (UserInfo user : users) {
            userListModel.addElement(user.name());
        }
    }

    public interface ChatActionListener {
        void onSendRequested(String text);

        void onLogoutRequested();

        void onChatError(String message);
    }
}