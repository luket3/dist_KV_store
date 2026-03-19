import java.util.HashMap;
import java.net.Socket;

public class Server implements Runnable {
    private static HashMap<String,String> store = new HashMap<>();

    private Socket socket;
    private String response;

    Server(Socket socket, String response) {
        this.socket = socket;
        this.response = response;
    }

    private void send_response() throws Exception {
        Comm comm = new Comm(socket);
        comm.send_string(response);
        comm.close_socket();
    }

    public static String execute_query(String query) {
        String[] split = query.split(" ");
        String res;

        if (split.length == 2 && split[0].equals("Get"))
            res = store.get(split[1]);
        else if (split.length == 3 && split[0].equals("Put"))
            res = store.put(split[1], split[2]);
        else if (split.length == 2 && split[0].equals("Delete"))
            res = store.remove(split[1]);
        else
            res = "null";

        if (res == null)
            res = "null";
        return res;
    }

    @Override
    public void run() {
        try {
            send_response();
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
