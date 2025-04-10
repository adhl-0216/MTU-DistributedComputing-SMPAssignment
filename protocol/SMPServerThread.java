package protocol;

import java.io.*;

public class SMPServerThread implements Runnable {
    private final SMPStreamSocket socket;
    private final SMPCommandHandler handler;
    // private final SessionManager sessionManager;

    public SMPServerThread(SMPStreamSocket socket, SMPCommandHandler handler, SMPSessionManager sessionManager) {
        this.socket = socket;
        this.handler = handler;
        // this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        try {
            boolean active = true;
            while (active) {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                // DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                byte command = input.readByte();
                byte length = input.readByte();
                byte[] payload = new byte[length];
                input.readFully(payload);

                switch (command) {
                    case 0x01 -> handler.handleLogin(socket, payload);
                    case 0x02 -> handler.handleUpload(socket, payload);
                    case 0x03 -> handler.handleDownloadAll(socket);
                    case 0x04 -> handler.handleDownloadOne(socket, payload);
                    case 0x05 -> {
                        handler.handleLogout(socket);
                        active = false;
                    }
                    default -> socket.sendError("Unknown command");
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}