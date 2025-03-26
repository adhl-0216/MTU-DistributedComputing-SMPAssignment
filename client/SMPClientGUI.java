import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SMPClientGUI {
    private SMPClientWrapper client;
    private JFrame frame;
    private JTextField usernameField, messageField, indexField;
    private JTextArea messageLog;
    private JButton loginButton, sendButton, downloadOneButton, downloadAllButton, logoutButton;

    public SMPClientGUI() {
        frame = new JFrame("SMP Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new BorderLayout());

        // Panel for login
        JPanel loginPanel = new JPanel();
        usernameField = new JTextField(15);
        loginButton = new JButton("Login");
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(loginButton);

        // Panel for message input
        JPanel messagePanel = new JPanel();
        messageField = new JTextField(20);
        sendButton = new JButton("Send");
        messagePanel.add(new JLabel("Message:"));
        messagePanel.add(messageField);
        messagePanel.add(sendButton);

        // Panel for download by index
        JPanel downloadPanel = new JPanel();
        indexField = new JTextField(5);
        downloadOneButton = new JButton("Download One");
        downloadAllButton = new JButton("Download All");
        logoutButton = new JButton("Logout");
        downloadPanel.add(new JLabel("Index:"));
        downloadPanel.add(indexField);
        downloadPanel.add(downloadOneButton);
        downloadPanel.add(downloadAllButton);
        downloadPanel.add(logoutButton);

        // Text area to display messages
        messageLog = new JTextArea(10, 30);
        messageLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageLog);

        frame.add(loginPanel, BorderLayout.NORTH);
        frame.add(messagePanel, BorderLayout.CENTER);
        frame.add(downloadPanel, BorderLayout.SOUTH);
        frame.add(scrollPane, BorderLayout.EAST);

        addActionListeners();
        frame.setVisible(true);
    }

    private void addActionListeners() {
        loginButton.addActionListener(e -> login());
        sendButton.addActionListener(e -> sendMessage());
        downloadOneButton.addActionListener(e -> downloadOne());
        downloadAllButton.addActionListener(e -> downloadAll());
        logoutButton.addActionListener(e -> logout());
    }

    private void login() {
        String username = usernameField.getText();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter a username!");
            return;
        }
        try {
            client = new SMPClientWrapper("localhost", 12345);
            client.connect();
            client.sendMessage((byte) 0x01, username.getBytes());
            messageLog.append("Logged in as: " + username + "\n");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Login failed!");
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter a message!");
            return;
        }
        try {
            client.sendMessage((byte) 0x02, message.getBytes());
            messageLog.append("Sent: " + message + "\n");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to send message!");
        }
    }

    private void downloadOne() {
        try {
            int index = Integer.parseInt(indexField.getText());
            client.sendMessage((byte) 0x04, new byte[]{(byte) index});
            byte[] response = client.receiveMessage();
            messageLog.append("Received (Index " + index + "): " + new String(response) + "\n");
        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to download message!");
        }
    }

    private void downloadAll() {
        try {
            client.sendMessage((byte) 0x03, new byte[0]);
            byte[] response = client.receiveMessage();
            messageLog.append("All Messages: " + new String(response) + "\n");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to download messages!");
        }
    }

    private void logout() {
        try {
            client.sendMessage((byte) 0x05, new byte[0]);
            client.close();
            messageLog.append("Logged out.\n");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Logout failed!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SMPClientGUI::new);
    }
}
