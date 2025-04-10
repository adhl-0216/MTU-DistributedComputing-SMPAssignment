package server;

import protocol.SMPServerWrapper;
import protocol.SMPSessionManager;

public class AppServer {
    private final SMPServerWrapper server;

    public AppServer(int port, int maxClients) {
        SMPSessionManager sessionManager = new AppSessionManager();
        AppCommandHandler handler = new AppCommandHandler(sessionManager);
        this.server = new SMPServerWrapper(port, maxClients, handler, sessionManager);
    }

    public void start() {
        server.start();
    }

    public void shutdown() {
        server.shutdown();
    }

    public static void main(String[] args) {
        AppServer appServer = new AppServer(12345, 10);
        appServer.start();
    }
}