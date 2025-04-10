package protocol;

import java.io.*;
import java.net.InetAddress;

public class SMPClientWrapper {
    private final String host;
    private final int port;
    private SMPStreamSocket streamSocket;

    public SMPClientWrapper(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        streamSocket = new SMPStreamSocket(InetAddress.getByName(host), port);
    }

    public void sendMessage(byte type, byte[] payload) throws IOException {
        if (streamSocket == null) {
            throw new IOException("Not connected.");
        }
        streamSocket.sendMessage(type, payload);
    }

    public byte[] receiveMessage() throws IOException {
        if (streamSocket == null) {
            throw new IOException("Not connected.");
        }
        DataInputStream input = new DataInputStream(streamSocket.getInputStream());
        byte type = input.readByte();  
        byte length = input.readByte();
        byte[] payload = new byte[length];
        input.readFully(payload);
        return payload;
    }

    public void close() throws IOException {
        if (streamSocket != null) {
            streamSocket.close();
        }
    }
}