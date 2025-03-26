import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class SMPClientWrapper {
    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    public SMPClientWrapper (String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    public void sendMessage(byte type, byte[] payload) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2 + payload.length);
        buffer.put(type);
        buffer.put((byte) payload.length);
        buffer.put(payload);
        output.write(buffer.array());
    }

    public byte[] receiveMessage() throws IOException {
        byte type = input.readByte();
        byte length = input.readByte();
        byte[] payload = new byte[length];
        input.readFully(payload);
        return payload;
    }

    public void close() throws IOException {
        socket.close();
    }
}