import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Machine {
    protected ServerSocket server_socket;

    public void create_listener(int port) throws IOException {
        try {
            server_socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("error createing server socket");
            throw e;
        }
    }

    public String listen() throws IOException {
        try {
            Socket socket = server_socket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());

            int length = in.readInt();
            byte[] buffer = new byte[length];
            in.readFully(buffer);

            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("failed to read message from server socket");
            throw e;
        }
    }

    public void send_string(String ip, int port, String message) throws Exception
    {
        try {
            Socket socket = new Socket(ip, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            out.writeInt(payload.length);
            out.write(payload);
            out.flush();
            socket.close();
        } catch (Exception e) {
            System.err.println("failed to send message to ip:" + ip + " and port:" + port);
            throw e;
        }
    }
}
