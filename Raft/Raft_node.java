import java.util.Map;

/**
 * Base class for Raft implementations holding common state.
 */
public class Raft_node {
    /** Current role type: "follower", "candidate", or "leader". */
    private Candidate candidate_role;
    private Follower follower_role;
    private Leader leader_role;
    private Raft_state raft_state;
    
    /**
     * Constructor initializes common Raft state.
     */
    public Raft_node(
            Map<String, Node> cluster_nodes,
            String id,
            Pipe state_machine_in
    ) {
        // Initialize shared state in Role base class
        raft_state = new Raft_state(cluster_nodes, id, state_machine_in);

        // Initialize role instances
        this.candidate_role = new Candidate(raft_state);
        this.follower_role = new Follower(raft_state);
        this.leader_role = new Leader(raft_state);
    }

    private String[] split_msg(String message) {

        String[] split = new String[7];
        int split_idx = 0;
        int start_idx = 0;
        String word = "";

        for (int i = 0; i <= message.length(); i++) {
            if (i == message.length() || message.charAt(i) == ' ') {
                word = message.substring(start_idx, i);
                split[split_idx] = word;
                split_idx++;
                start_idx = i + 1;
            }
            else if (message.charAt(i) == '[') {
                word = message.substring(i);
                split[split_idx] = word;
                break;
            }
        }

        return split;
    }

    /**
     * Start processing an incoming connection representing an RPC.
     *
     * @param message the RPC message to process
     */
    public void Handle_message(String message) {

        // make command seperate
        String[] parts = split_msg(message);
        String rpc_type = parts[0];

        // handle request vote, append entries and client command
        // this this.term is higher functions return
        if (rpc_type.equals("AppendEntries"))
            follower_role.append_entries(parts);
        else if (rpc_type.equals("RequestVote"))
            follower_role.request_vote(parts);
        else if (rpc_type.equals("ClientCommand") 
                    && !raft_state.type.equals("leader"))
            follower_role.hand_to_leader(message);

        if (raft_state.type.equals("leader")) {
            if (rpc_type.equals("AppendEntriesReply")) {
                leader_role.append_entries(parts);
            } else if (rpc_type.equals("ClientCommand")){
                // Handle client command
                leader_role.process_client_command(message);
            }
        } else if (raft_state.type.equals("candidate")) {
            if (rpc_type.equals("RequestVoteReply")) {
                candidate_role.request_vote(parts);
            }
        }
    }

    public void send_heartbeat() {
        this.leader_role.broadcast_append_entries();
    }

    public String get_role() {
        return raft_state.type;
    }

    public void start_election() {
        // Transition to candidate state and start election process
        this.candidate_role.start_election();
    }
}