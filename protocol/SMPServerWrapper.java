package protocol;

import javax.net.ssl.*;
import java.io.*;
import java.util.concurrent.*;

public class SMPServerWrapper {
    private final int port;
    private final ExecutorService threadPool;
    private SSLServerSocket serverSocket;
    private final SMPCommandHandler handler;
    private final SMPSessionManager sessionManager;

    public SMPServerWrapper(int port, int maxClients, SMPCommandHandler handler, SMPSessionManager sessionManager) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(maxClients);
        this.handler = handler;
        this.sessionManager = sessionManager;
    }

    public void start() {
        try {
            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket) factory.createServerSocket(port);
            System.out.println("SMP Server ready on port " + port);

            while (true) {
                System.out.println("Waiting for a connection.");
                SSLSocket sslSocket = (SSLSocket) serverSocket.accept();
                SMPStreamSocket smpSocket = new SMPStreamSocket(sslSocket);
                System.out.println("Connection accepted");
                threadPool.execute(new SMPServerThread(smpSocket, handler, sessionManager));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
            System.out.println("Server shut down.");
        } catch (IOException e) {
            System.out.println("Error while shutting down: " + e.getMessage());
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}