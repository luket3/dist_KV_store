import java.util.Map;

/**
 * Base class for Raft node roles (Follower, Leader, Candidate).
 */
public abstract class Role {

    public static int term;
    public static String id;
    public static Map<String, Node> nodes;
    public static Raft_log log;
    public static Node leader;
    public static String voted_for;
    public static int number_of_nodes;
    public static String type;

    public Role() {
    }

    /**
     * Initialize common Raft state.
     */
    public static void initializeState(Map<String, Node> cluster_nodes, String node_id, Pipe state_machine_in) {
        Role.nodes = cluster_nodes;
        Role.id = node_id;
        Role.log = new Raft_log(state_machine_in);
        Role.term = 0;
        Role.voted_for = null;
        Role.leader = null;
        Role.number_of_nodes = cluster_nodes.size();
        Role.type = "follower";
    }

    /**
     * Send a message to all other nodes in the cluster.
     *
     * @param message the message to broadcast
     */
    public void broadcast(String message) {
        System.out.println("Broadcasting message to all nodes: " + 
                            message);

        Comm comm = new Comm();
        for (Node node : nodes.values()) {
            if (!node.id.equals(Role.id)) {
                try {
                    comm.create_socket(node.ip, node.port);
                    comm.send_string(message);
                    comm.close_socket();
                } catch (Exception e) {
                    System.err.println("Failed to send message to node " + node.id);
                }
            }
        }
    }

    /**
     * Send a single message to the specified node.
     *
     * @param node target node
     * @param message message to send
     */
    protected void send_to_node(Node node, String message) {
        System.out.println("Sending message to node " + 
                            node.id + ": " + 
                            message);
        try {
             Comm comm = new Comm();
             comm.create_socket(node.ip, node.port);
             comm.send_string(message);
             comm.close_socket();
        } catch (Exception e) {
            System.err.println("Failed to send message to node " + node.id);
        }
    }
}
