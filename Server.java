import java.util.HashMap;

public class Server implements Runnable {
    private static HashMap<String,String> store;

    private String query_ip;
    private int query_port;
    private String response;

    Server(String query_ip, int query_port, String response) {
        this.query_ip = query_ip;
        this.query_port = query_port;
        this.response = response;
    }

    private void send_response() throws Exception {
        Comm comm = new Comm();
        comm.create_socket(query_ip, query_port);
        comm.send_string(response);
        comm.close_socket();
    }

    public static String execute_query(String query) {
        String[] split = query.split(" ");
        String res = "True";

        if (split[0].equals("Get")) {
            res = store.get(split[1]);
        } else if (split[0].equals("Put")) {
            store.put(split[1], split[2]);
        } else if (split[0].equals("Delete")) {
            store.remove(split[1]);
        }

        return res;
    }

    @Override
    public void run() {
        try {
            send_response();
        } catch(Exception e) {
            System.err.println("failed to send response");
        }
    }
}
