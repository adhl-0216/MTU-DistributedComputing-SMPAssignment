package protocol;

import java.io.*;

public interface SMPCommandHandler {
    void handleLogin(SMPStreamSocket socket, byte[] payload) throws IOException;
    void handleUpload(SMPStreamSocket socket, byte[] payload) throws IOException;
    void handleDownloadAll(SMPStreamSocket socket) throws IOException;
    void handleDownloadOne(SMPStreamSocket socket, byte[] payload) throws IOException;
    void handleLogout(SMPStreamSocket socket) throws IOException;

    SMPSessionManager getSessionManager();
}