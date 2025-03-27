
import java.io.*;
import java.net.*;
import java.util.*;

public class SMPAppServer extends SMPServerWrapper {
    private final Map<Socket, String> userSessions = Collections.synchronizedMap(new HashMap<>());
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());

    public SMPAppServer(int port, int maxClients) {
        super(port, maxClients);
    }

    @Override
    protected void handleLogin(Socket client, byte[] payload) {
        String user = new String(payload);
        userSessions.put(client, user);
        System.out.println(user + " logged in.");
    }

    @Override
    protected void handleUpload(Socket client, byte[] payload) {
        String user = userSessions.get(client);
        if (user == null) {
            sendError(client, "Not logged in.");
            return;
        }
        String message = user + ": " + new String(payload);
        messages.add(message);
        System.out.println("Message received: " + message);
        System.out.println("Current messages:");
        for (int i = 0; i < messages.size(); i++) {
            System.out.println(i + ": " + messages.get(i));
        }
    }

    @Override
    protected void handleDownloadAll(Socket client) {
        try {
            if (messages.isEmpty()) {
                sendError(client, "No messages available.");
                return;
            }
            String allMessages = String.join("\n", messages);
            System.out.println("Downloading all messages:\n" + allMessages); // Debug output
            sendMessage(client, (byte) 0x10, allMessages.getBytes());
        } catch (IOException e) {
            sendError(client, "Failed to send messages.");
        }
    }
    

    @Override
    protected void handleDownloadOne(Socket client, byte[] payload) {
        try {
            int index = payload[0];
            if (index < 0 || index >= messages.size()) {
                sendError(client, "Invalid message index.");
                return;
            }
            System.out.println("Downloading message at index " + index + ": " + messages.get(index)); // Debug output
            sendMessage(client, (byte) 0x10, messages.get(index).getBytes());
        } catch (IOException e) {
            sendError(client, "Failed to retrieve message.");
        }
    }

    @Override
    protected void handleLogout(Socket client) {
        userSessions.remove(client);
        System.out.println("User logged out.");
    }

    public static void main(String[] args) {
        SMPAppServer server = new SMPAppServer(12345, 10);
        server.start();
    }
}
