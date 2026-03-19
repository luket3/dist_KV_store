import java.net.ServerSocket;
import java.net.Socket;

public class Comm_server extends Comm_virtual {
    private ServerSocket server_socket;

    public void create_socket(int port) throws Exception {
        server_socket = new ServerSocket(port);
    }

    @Override
    public void close_socket() throws Exception {
        server_socket.close();
    }

    public Socket listen_for_connection() throws Exception {
        Socket socket = server_socket.accept();            
        return socket;
    }
}
