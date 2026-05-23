/*
 * File: Server.java
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Per-connection server worker that executes simple
 * key-value queries.
 */

import java.util.HashMap;
import java.net.Socket;

/**
 * Per-connection server worker that executes simple key-value queries.
 *
 * <p>Supported queries: {@code Get key}, {@code Put key value},
 * and {@code Delete key}.</p>
 */
public class Server implements Runnable {
    /** Shared in-memory key-value store. */
    private static HashMap<String,String> store = new HashMap<>();

    /** Identifier of the node servicing this request. */
    private String node_id;

    /** Communication helper wrapping the accepted socket. */
    private Comm comm;

    /**
     * Create a new worker for the given connection and node id.
     *
     * @param socket accepted client socket
     * @param node_id id of the node handling the request
     */
    Server(Socket socket, String node_id) {
        this.node_id = node_id;
        comm = new Comm(socket);
    }

    /**
     * Send a response string back to the requester and close the socket.
     *
     * @param response string to send
     * @throws Exception on communication errors
     */
    private void send_response(String response) throws Exception {
        comm.send_string(response);
        comm.close_socket();
    }

    /**
     * Execute a simple key-value query against the shared store.
     *
     * @param query textual query to execute
     * @return result string or "null" when no value exists / invalid query
     */
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
