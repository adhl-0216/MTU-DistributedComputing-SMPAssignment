package protocol;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;

public class SMPStreamSocket extends Socket {
    private final SSLSocket sslSocket;
    private DataInputStream input;
    private DataOutputStream output;

    public SMPStreamSocket(SSLSocket sslSocket) throws IOException {
        this.sslSocket = sslSocket;
        setStreams();
    }

    public SMPStreamSocket(InetAddress host, int port) throws IOException {
        super();
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        this.sslSocket = (SSLSocket) factory.createSocket(host, port);
        sslSocket.startHandshake();
        setStreams();
    }

    private void setStreams() throws IOException {
        input = new DataInputStream(sslSocket.getInputStream());
        output = new DataOutputStream(sslSocket.getOutputStream());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return input;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return output;
    }

    @Override
    public InetAddress getInetAddress() {
        return sslSocket.getInetAddress();
    }

    @Override
    public void close() throws IOException {
        try {
            input.close();
            output.close();
        } finally {
            sslSocket.close();
        }
    }

    public void sendMessage(byte command, byte[] payload) throws IOException {
        output.writeByte(command);
        output.writeByte(payload.length);
        output.write(payload);
        output.flush();
    }

    public void sendError(String errorMessage) throws IOException {
        sendMessage((byte) 0xFF, errorMessage.getBytes());
    }

    // Override additional Socket methods as needed to delegate to sslSocket
    @Override
    public int getPort() {
        return sslSocket.getPort();
    }

    @Override
    public InetAddress getLocalAddress() {
        return sslSocket.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return sslSocket.getLocalPort();
    }

    @Override
    public boolean isConnected() {
        return sslSocket.isConnected();
    }

    @Override
    public boolean isClosed() {
        return sslSocket.isClosed();
    }
}