import java.util.HashMap;
import java.net.Socket;

public class Server implements Runnable {
    private static HashMap<String,String> store = new HashMap<>();

    private String node_id;
    private Comm comm;

    Server(Socket socket, String node_id) {
        this.node_id = node_id;
        comm = new Comm(socket);
    }

    private void send_response(String response) throws Exception {
        comm.send_string(response);
        comm.close_socket();
    }

    public String execute_query(String query) {
        String[] split = query.split(" ");
        String res;

        if (split.length == 2 && split[0].equals("Get"))
            synchronized(store) {res = store.get(split[1]);}
        else if (split.length == 3 && split[0].equals("Put"))
            synchronized(store) {res = store.put(split[1], split[2]);}
        else if (split.length == 2 && split[0].equals("Delete"))
            synchronized(store) {res = store.remove(split[1]);}
        else
            res = "null";

        if (res == null)
            res = "null";
        return res;
    }

    @Override
    public void run() {
        try {
            String query = comm.read_string();
            System.out.println("Node:" + node_id + " executing query:" + query);
            send_response(execute_query(query));
        } 
        catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
