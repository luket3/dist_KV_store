
/**
 * Base class for Raft node roles (Follower, Leader, Candidate).
 */
public abstract class Role {

    Raft_state raft_state;

    public Role(Raft_state raft_state) {
        this.raft_state = raft_state;
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
            System.err.println("Failed to send message to node "
                    + node.id);
        }
    }
}
