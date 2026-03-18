
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
        String query = comm.listen();
        String response = Server.execute_query(query);
        Thread t = new Thread(new Server(comm.remote_ip, comm.remote_port, response));
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