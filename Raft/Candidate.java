/**
 * Candidate role for Raft consensus.
 */
public class Candidate extends Role {

    private int votes_received;

    public Candidate(Raft_state raft_state) {
        super(raft_state);
        this.votes_received = 0;
    }

    public boolean request_vote(String[] message_parts) {
        // Handle a RequestVote response while this node is a candidate.
        // Format: RequestVote <term> <senderID> <voteGranted>
        if (message_parts.length < 3) {
            // Invalid message format
            return true; // Treat as no vote granted
        }
        int voter_term = Integer.parseInt(message_parts[1]);
        String sender_id = message_parts[2];
        boolean vote_granted = Boolean.parseBoolean(message_parts[3]);

        // Update term and revert to follower if we see a higher term
        if (voter_term > raft_state.term) {
            raft_state.term = voter_term;
            raft_state.type = "follower";
            raft_state.voted_for = null;
            return false;
        }

        // Check if we have won the election
        this.votes_received += vote_granted ? 1 : 0;
        if (this.votes_received > raft_state.number_of_nodes / 2) {
            raft_state.type = "leader";
        }
        return true;
    }

    public void start_election() {
        // Increment term and transition this node into candidate state.
        raft_state.term++;
        raft_state.type = "candidate";
        // Vote for self as the current candidate.
        raft_state.voted_for = raft_state.id;
        // In a real implementation this would be the node's own identifier
        this.votes_received = 1; // Vote for self

        // Broadcast RequestVote RPCs to the other cluster nodes.
        broadcast("RequestVote " + 
               raft_state.term + " " + 
               raft_state.id + " " + 
               raft_state.log.get_last_idx() + " " + 
               raft_state.log.get_last_term());
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
        for (Node node : raft_state.nodes.values()) {
            if (!node.id.equals(raft_state.id)) {
                try {
                    comm.create_socket(node.ip, node.port);
                    comm.send_string(message);
                    comm.close_socket();
                } catch (Exception e) {
                    System.err.println("Failed to send message to node "
                            + node.id);
                }
            }
        }
    }
}
