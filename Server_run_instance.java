
public class Server_run_instance {
    public static Comm_server comm;
    public static String node_id;
    public static int port;

    public static void init(String args[]) {
        comm = new Comm_server();
        node_id = args[0];
        port = Integer.parseInt(args[1]);
    }

    public static void create_work() throws Exception {
        Thread t = new Thread(new Server(comm.listen_for_connection(), node_id));
        t.start();
    }

    public static void main(String[] args) throws Exception {
        Server_run_instance.init(args);

        comm.create_socket(port);
        while (true) {
            create_work();
        }
    }
}