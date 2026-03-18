import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Comm extends Comm_virtual {
    private Socket socket;

    public void create_socket(String ip, int port) throws Exception {
        socket = new Socket(ip, port);
    }

    @Override
    public void close_socket() throws Exception {
        socket.close();
    }

    @Override
    public String listen() throws Exception {
        return read_string(socket);
    }

    public void send_string(String message) throws Exception
    {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            out.writeInt(payload.length);
            out.write(payload);
            out.flush();

            out.close();
        } catch (Exception e) {
            throw e;
        }
    }
}
