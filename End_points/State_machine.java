/*
 * File: Server.java
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Per-connection server worker that executes simple
 * key-value queries.
 */

import java.util.HashMap;

/**
 * Per-connection server worker that executes simple key-value queries.
 *
 * <p>Supported queries: {@code Get key}, {@code Put key value},
 * and {@code Delete key}.</p>
 */
public class State_machine implements Runnable {
    /** Shared in-memory key-value store. */
    private static HashMap<String,String> store = new HashMap<>();
    Comm comm;
    Pipe in_pipe;
    String node_id;

    HashMap<String, Comm> client_info = new HashMap<>();
    String return_code;


    State_machine(Pipe pipe, String node_id) throws Exception {
        this.return_code = "0";
        this.in_pipe = pipe;
        this.node_id = node_id;
        this.comm = new Comm();
    }

    private void send_response(String response) throws Exception {
        Comm client = client_info.get(return_code);
        if (client == null) {
            return;
        }

        System.out.println("Node:" + node_id + " sending response:" + response);
        client.send_string(response);
        client.close_socket();
        client_info.remove(return_code);
    }

    /**
     * Execute a simple key-value query against the shared store.
     *
     * @param query textual query to execute
     * @return result string or "null" when no value exists / invalid query
     */
    public String parse_query(Message_info query) {
        String[] split = query.message.split(" ");
        String res;
        
        return_code = split[0];
        String message_type = split[1];

        if (message_type.equals("Reply")) {
            client_info.put(return_code, query.comm);
            return "no response";
        }

        if (message_type.equals("Get"))
            res = store.get(split[2]);
        else if (message_type.equals("Put"))
            res = store.put(split[2], split[3]);
        else if (message_type.equals("Delete"))
            res = store.remove(split[2]);
        else
            res = "null";

        if (res == null)
            res = "null";
        return res;
    }

    @Override
    public void run() {

        while (true) {
            try {
                Message_info query = in_pipe.take_all();
                String logMessage = "Node:" + node_id + " recieved command:"
                        + query.message;
                System.out.println(logMessage);

                String response = parse_query(query);

                if (!response.equals("no response")) {
                    send_response(response);
                }
            }
            catch(Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
