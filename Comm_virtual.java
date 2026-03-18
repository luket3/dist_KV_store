import java.io.DataInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Comm_virtual {

    protected String read_string(Socket socket) throws Exception {
        DataInputStream in = new DataInputStream(socket.getInputStream());

        int length = in.readInt();
        byte[] buffer = new byte[length];
        in.readFully(buffer);

        return new String(buffer, StandardCharsets.UTF_8);
    }

    public void create_socket() throws Exception {}

    public void close_socket() throws Exception {}

    public String listen() throws Exception {return null;}
}
