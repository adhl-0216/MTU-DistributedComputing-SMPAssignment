
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
    }

    @Override
    protected void handleDownloadAll(Socket client) {
        try {
            for (String msg : messages) {
                sendMessage(client, (byte) 0x10, msg.getBytes());
            }
            sendMessage(client, (byte) 0x11, new byte[0]); // End marker
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
