import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Comm_server extends Comm_virtual {
    private ServerSocket server_socket;
    public String remote_ip;
    public int remote_port;


    public void create_socket(int port) throws Exception {
        try {
            server_socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("error createing server socket");
            throw e;
        }
    }

    @Override
    public void close_socket() throws Exception {
        try {
            server_socket.close();
        } catch (IOException e) {
            System.err.println("error closing server socket");
            throw e;
        }
    }

    @Override
    public String listen() throws Exception {
        try {
            Socket socket = server_socket.accept();
            remote_ip = socket.getInetAddress().getHostAddress();
            remote_port = socket.getPort();
            
            return read_string(socket);
        } catch (Exception e) {
            System.err.println("failed to read message from server socket");
            throw e;
        }
    }
}
