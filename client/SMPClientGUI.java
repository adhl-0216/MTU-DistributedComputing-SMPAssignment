package client;

import protocol.SMPClientWrapper;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SMPClientGUI extends JFrame {
    private SMPClientWrapper client;
    private JTextField usernameField, messageField;
    private JTextArea messageLog;
    private JButton loginButton, sendButton, downloadOneButton, downloadAllButton, logoutButton, clearLogButton;
    private boolean isLoggedIn = false;

    public SMPClientGUI() {
        setTitle("SMP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int windowWidth = (int) (screenSize.getWidth() * 0.45); 
        setMinimumSize(new Dimension(windowWidth, 400));
        setPreferredSize(new Dimension(windowWidth, 400));

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernameField = new JTextField(15);
        loginButton = new JButton("Login");
        logoutButton = new JButton("Logout");
        topPanel.add(new JLabel("Username:"));
        topPanel.add(usernameField);
        topPanel.add(loginButton);
        topPanel.add(logoutButton);

        messageLog = new JTextArea(15, 40);
        messageLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageLog);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel sendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messageField = new JTextField(20);
        sendButton = new JButton("Send");
        sendPanel.add(new JLabel("Message:"));
        sendPanel.add(messageField);
        sendPanel.add(sendButton);

        JPanel downloadPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        downloadOneButton = new JButton("Download One");
        downloadAllButton = new JButton("Download All");
        clearLogButton = new JButton("Clear Log"); // New button
        downloadPanel.add(downloadOneButton);
        downloadPanel.add(downloadAllButton);
        downloadPanel.add(clearLogButton);

        bottomPanel.add(sendPanel, BorderLayout.WEST);
        bottomPanel.add(downloadPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        updateComponentStates();

        addActionListeners();
        pack();
        setVisible(true);
    }

    private void addActionListeners() {
        loginButton.addActionListener(e -> login());
        sendButton.addActionListener(e -> sendMessage());
        downloadOneButton.addActionListener(e -> downloadOne());
        downloadAllButton.addActionListener(e -> downloadAll());
        logoutButton.addActionListener(e -> logout());
        clearLogButton.addActionListener(e -> clearLog());
    }

    private void updateComponentStates() {
        messageField.setEnabled(isLoggedIn);
        sendButton.setEnabled(isLoggedIn);
        downloadOneButton.setEnabled(isLoggedIn);
        downloadAllButton.setEnabled(isLoggedIn);
        logoutButton.setEnabled(isLoggedIn);
        loginButton.setEnabled(!isLoggedIn);
        clearLogButton.setEnabled(isLoggedIn);
    }

    private void login() {
        String username = usernameField.getText();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a username!");
            return;
        }
        try {
            client = new SMPClientWrapper("localhost", 12345);
            client.connect();
            client.sendMessage((byte) 0x01, username.getBytes());
            byte[] response = client.receiveMessage();
            messageLog.append(new String(response) + " as: " + username + "\n");
            isLoggedIn = true;
            updateComponentStates();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a message!");
            return;
        }
        try {
            client.sendMessage((byte) 0x02, message.getBytes());
            messageLog.append("Sent: " + message + "\n");
            messageField.setText("");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to send message: " + ex.getMessage());
        }
    }

    private void downloadOne() {
        String indexStr = JOptionPane.showInputDialog(this, "Enter message index to download:", "Download One", JOptionPane.QUESTION_MESSAGE);
        if (indexStr == null || indexStr.trim().isEmpty()) {
            return;
        }
        try {
            int index = Integer.parseInt(indexStr);
            if (index < 0) {
                JOptionPane.showMessageDialog(this, "Index must be non-negative!");
                return;
            }
            client.sendMessage((byte) 0x04, new byte[]{(byte) index});
            byte[] response = client.receiveMessage();
            messageLog.append("Message @ Index(" + index + "): " + new String(response) + "\n");
        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Failed to download message: " + ex.getMessage());
        }
    }

    private void downloadAll() {
        try {
            client.sendMessage((byte) 0x03, new byte[0]);
            byte[] response = client.receiveMessage();
            messageLog.append("====================================================================================\n");
            messageLog.append("  All Messages\n");
            messageLog.append("====================================================================================\n");
            String[] messages = new String(response).split("\n");
            for (int i = 0; i < messages.length; i++) {
                messageLog.append("    Message @ Index(" + i + "): " + messages[i] + "\n");
            }
            messageLog.append("====================================================================================\n");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to download messages: " + ex.getMessage());
        }
    }

    private void logout() {
        try {
            client.sendMessage((byte) 0x05, new byte[0]);
            byte[] response = client.receiveMessage();
            messageLog.append("Logged out: " + new String(response) + "\n");
            client.close();
            client = null;
            isLoggedIn = false;
            updateComponentStates();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Logout failed: " + ex.getMessage());
        }
    }

    private void clearLog() {
        messageLog.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SMPClientGUI::new);
    }
}