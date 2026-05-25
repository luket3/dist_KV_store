import java.util.Map;

/**
 * Base class for Raft implementations holding common state.
 */
public class RaftNode {
    /** Current role type: "follower", "candidate", or "leader". */
    private Candidate candidate_role;
    private Follower follower_role;
    private Leader leader_role;
    
    /**
     * Constructor initializes common Raft state.
     */
    public RaftNode(Map<String, Node> cluster_nodes, String id, Pipe state_machine_in) {
        // Initialize shared state in Role base class
        Role.initializeState(cluster_nodes, id, state_machine_in);

        // Initialize role instances
        this.candidate_role = new Candidate();
        this.follower_role = new Follower();
        this.leader_role = new Leader();
    }

    /**
     * Start processing an incoming connection representing an RPC.
     *
     * @param message the RPC message to process
     */
    public void Handle_message(String message) {

        String[] parts = message.split(" ");
        String rpc_type = parts[0];

        if (Role.type.equals("follower")) {
                if (rpc_type.equals("AppendEntries")) {
                    follower_role.append_entries(parts);
                } else if (rpc_type.equals("RequestVote")) {
                    follower_role.request_vote(parts);
                } else {
                    follower_role.hand_to_leader(message);
                }
        } else if (Role.type.equals("leader")) {
            if (rpc_type.equals("AppendEntries")) {
                leader_role.append_entries(parts);
            } else if (rpc_type.equals("RequestVote")) {
                // Leaders can also receive RequestVote RPCs and should
                // process them as followers
                follower_role.request_vote(parts);
            } else {
                // Handle client command
                leader_role.process_client_command(parts);
            }
        } else if (Role.type.equals("candidate")) {
            if (rpc_type.equals("AppendEntries")) {
                follower_role.append_entries(parts);
            } else if (rpc_type.equals("RequestVote")) {
                if (!candidate_role.request_vote(parts)) {
                    follower_role.request_vote(parts);
                }
            } else {
                follower_role.hand_to_leader(message);
            }
        }
    }

    public void send_heartbeat() {
        this.leader_role.send_append_entries();
    }

    public String get_role() {
        return Role.type;
    }

    public void start_election() {
        // Transition to candidate state and start election process
        this.candidate_role.start_election();
    }
}