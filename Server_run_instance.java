
/**
 * Server runner program that initializes listening sockets and spawns a
 * {@link Server} worker thread for each incoming connection.
 */
public class Server_run_instance {
    public static Request_handler request_handler;
    public static consistent_hash_map nodes;
    public static String node_id;
    public static int port;


    /**
     * Initialize static runner state from command-line arguments.
     *
     * @param args expected to contain {@code node_id} and {@code port}
     */
    public static void init(String args[]) {
        request_handler = new Request_handler();
        node_id = args[0];
        port = Integer.parseInt(args[1]);
    }

    /**
     * Accept a connection and start a worker thread to handle it.
     *
     * @throws Exception on socket accept or thread creation errors
     */
    public static void create_work() throws Exception {
        Thread t = new Thread(new Server(request_handler.listen_for_connection(), node_id));
        t.start();
    }

    /**
     * Main entry point for the server process.
     *
     * @param args command-line arguments: {@code node_id} {@code port}
     * @throws Exception on initialization or runtime socket errors
     */
    public static void main(String[] args) throws Exception {
        Server_run_instance.init(args);

        request_handler.create_socket(port);
        while (true) {
            create_work();
        }
    }
}