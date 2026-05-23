import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

/**
 * Base class for Raft node roles (Follower, Leader, Candidate).
 */
public abstract class Role {

    public static int term;
    public static String id;
    public static Map<String, Node> nodes;
    public static ArrayList<log_entry> log;
    public static int commit_index;
    public static Node leader;
    public static String voted_for;
    public static int number_of_nodes;
    public static int prev_log_index;
    public static int prev_log_term;
    public static Map<Integer, Integer> votes_received_per_index;
    public static String committed_entries;
    public static String type;

    public Role() {
    }

    /**
     * Initialize common Raft state.
     */
    public static void initializeState(Map<String, Node> cluster_nodes, String node_id) {
        Role.nodes = cluster_nodes;
        Role.id = node_id;
        Role.log = new ArrayList<>();
        Role.term = 0;
        Role.commit_index = 0;
        Role.voted_for = null;
        Role.leader = null;
        Role.number_of_nodes = cluster_nodes.size();
        Role.prev_log_index = -1;
        Role.prev_log_term = -1;
        Role.votes_received_per_index = new HashMap<>();
        Role.committed_entries = "";
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
        try {
             Comm comm = new Comm();
             for (Node node : nodes.values()) {
                 if (!node.id.equals(Role.id)) { // Don't send to self
                     comm.create_socket(node.ip, node.port);
                     comm.send_string(message);
                     comm.close_socket();
                 }
             }
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
