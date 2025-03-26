import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class SMPServerWrapper {
    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());

    public SMPServerWrapper(int port, int maxClients) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(maxClients);
    }

    protected abstract void handleLogin(Socket client, byte[] payload);
    protected abstract void handleUpload(Socket client, byte[] payload);
    protected abstract void handleDownloadAll(Socket client);
    protected abstract void handleDownloadOne(Socket client, byte[] payload);
    protected abstract void handleLogout(Socket client);

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            System.out.println("SMP Server started on port " + port);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Server stopped: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("Server shut down.");
        } catch (IOException e) {
            System.out.println("Error while shutting down: " + e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                boolean active = true;
                while (active) {
                    byte command = input.readByte();
                    byte length = input.readByte();
                    byte[] payload = new byte[length];
                    input.readFully(payload);

                    switch (command) {
                        case 0x01 -> handleLogin(socket, payload);
                        case 0x02 -> handleUpload(socket, payload);
                        case 0x03 -> handleDownloadAll(socket);
                        case 0x04 -> handleDownloadOne(socket, payload);
                        case 0x05 -> {
                            handleLogout(socket);
                            active = false;
                        }
                        default -> sendError(socket, "Unknown command");
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + socket.getInetAddress());
            }
        }
    }

    protected void sendMessage(Socket client, byte command, byte[] payload) throws IOException {
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        output.writeByte(command);
        output.writeByte(payload.length);
        output.write(payload);
    }

    protected void sendError(Socket client, String errorMessage) {
        try {
            sendMessage(client, (byte) 0xFF, errorMessage.getBytes());
        } catch (IOException e) {
            System.out.println("Failed to send error: " + e.getMessage());
        }
    }
}
