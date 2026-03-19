import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Comm {
    private Socket socket;

    Comm(Socket socket) {
        this.socket = socket;
    }

    Comm() {}

    private String read_string(Socket socket) throws Exception {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        int length = in.readInt();
        byte[] buffer = new byte[length];
        
        in.readFully(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public void close_socket() throws Exception {
        socket.close();
    }

    public void create_socket(String ip, int port) throws Exception {
        socket = new Socket(ip, port);
    }

    public String listen_for_string() throws Exception {
        return read_string(socket);
    }

    public void send_string(String message) throws Exception
    {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);

        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }
}
