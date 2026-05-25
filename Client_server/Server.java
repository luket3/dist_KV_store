/*
 * File: Server_run_instance.java
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Server runner program that initializes listening sockets
 * and spawns a Server worker thread for each incoming connection.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Server runner program that initializes listening sockets and spawns a
 * {@link State_machine} worker thread for each incoming connection.
 */
public class Server {
    public static Request_handler request_handler;
    public static consistent_hash_map nodes;
    public static String node_id;
    public static int port;
    public static Map<String, Node> node_map;
    public static Pipe raft_in;
    public static Pipe state_machine_in;
    public static int return_code;


    /**
     * Initialize static runner state from command-line arguments.
     *
     * @param args expected to contain {@code node_id} and {@code port}
     */
    public static void init(String args[]) {
        request_handler = new Request_handler();
        node_id = args[0];
        port = Integer.parseInt(args[1]);
        return_code = 0;

        node_map = new HashMap<>();

        try {
        List<String> file_data =
            Files.readAllLines(Paths.get("network.config"));

            for (String line : file_data) {
            String[] split = line.split(",");
            node_map.put(split[0], new Node(
                split[0],
                split[1],
                Integer.parseInt(split[2])
                )
            );
        }
        }
        catch (Exception e) {
            System.out.println("Error reading network configuration: " + e);
            System.exit(1);
        }

        raft_in = new Pipe();
        state_machine_in = new Pipe();
    }

    /**
     * Accept a connection and parse it to raft via the pipe.
     *
     * @throws Exception on socket accept or thread creation errors
     */
    public static void hand_off() throws Exception {

        Comm comm = new Comm(request_handler.listen_for_connection());
        String request = comm.read_string();

        String message_type = request.substring(0, request.indexOf(" "));
        if (!message_type.equals("AppendEntries") && !message_type.equals("RequestVote") && !message_type.equals("ClientCommand")) {
            request = Integer.toString(return_code) + " " + request;

            state_machine_in.put(new message_info(Integer.toString(return_code) + " Reply", comm));
            return_code += 1;
        }

        raft_in.put(request);
    }

    /**
     * Main entry point for the server process.
     *
     * @param args command-line arguments: {@code node_id} {@code port}
     * @throws Exception on initialization or runtime socket errors
     */
    public static void main(String[] args) throws Exception {
        Server.init(args);

        // start Raft instance in separate thread to handle cluster communication
        Thread raft = new Thread(new Raft(raft_in, state_machine_in, node_map, node_id));
        raft.start();

        // start state machine instance in separate thread to handle client queries
        Thread state_machine = new Thread(new State_machine(state_machine_in, node_id));
        state_machine.start();


        /*
         * Main server loop: listen for incoming connections and pass to Raft
         */
        request_handler.create_socket(port);
        while (true) {
            hand_off();
        }
    }
}