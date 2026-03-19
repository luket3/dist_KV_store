import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Comm extends Comm_virtual {
    private Socket socket;

    Comm(Socket socket) {
        this.socket = socket;
    }

    Comm() {}

    public void create_socket(String ip, int port) throws Exception {
        socket = new Socket(ip, port);
    }

    @Override
    public void close_socket() throws Exception {
        socket.close();
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
