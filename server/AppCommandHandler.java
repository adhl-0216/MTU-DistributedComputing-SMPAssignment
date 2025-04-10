package server;

import protocol.SMPStreamSocket;
import protocol.SMPSessionManager;
import protocol.SMPCommandHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class AppCommandHandler implements SMPCommandHandler {
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
    private final SMPSessionManager sessionManager;

    public AppCommandHandler(SMPSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void handleLogin(SMPStreamSocket socket, byte[] payload) throws IOException {
        String username = new String(payload);
        if (sessionManager.getUsername(socket).isPresent()) {
            socket.sendError("Already logged in.");
            return;
        }
        sessionManager.loginUser(socket, username);
        socket.sendMessage((byte) 0x01, "Login successful".getBytes());
    }

    @Override
    public void handleUpload(SMPStreamSocket socket, byte[] payload) throws IOException {
        String username = sessionManager.getUsername(socket)
            .orElseThrow(() -> new IOException("Not logged in."));
        String message = username + ": " + new String(payload);
        messages.add(message);
        System.out.println("Message received: " + message);
        System.out.println("Current messages:");
        for (int i = 0; i < messages.size(); i++) {
            System.out.println(i + ": " + messages.get(i));
        }
    }

    @Override
    public void handleDownloadAll(SMPStreamSocket socket) throws IOException {
        if (messages.isEmpty()) {
            socket.sendError("No messages available.");
            return;
        }
        String allMessages = String.join("\n", messages);
        System.out.println("Downloading all messages:\n" + allMessages);
        socket.sendMessage((byte) 0x10, allMessages.getBytes());
    }

    @Override
    public void handleDownloadOne(SMPStreamSocket socket, byte[] payload) throws IOException {
        int index = payload[0];
        if (index < 0 || index >= messages.size()) {
            socket.sendError("Invalid message index.");
            return;
        }
        System.out.println("Downloading message at index " + index + ": " + messages.get(index));
        socket.sendMessage((byte) 0x10, messages.get(index).getBytes());
    }

    @Override
    public void handleLogout(SMPStreamSocket socket) throws IOException {
        if (!sessionManager.getUsername(socket).isPresent()) {
            socket.sendError("Not logged in.");
            return;
        }
        sessionManager.logoutUser(socket);
        socket.sendMessage((byte) 0x05, "Logout successful".getBytes());
    }

    @Override
    public SMPSessionManager getSessionManager() {
        return sessionManager;
    }
}