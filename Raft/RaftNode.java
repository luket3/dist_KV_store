import java.util.HashMap;
import java.util.ArrayList;
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
    public RaftNode(Map<String, Node> cluster_nodes, String id) {
        // Initialize shared state in Role base class
        Role.initializeState(cluster_nodes, id);
        
        // Initialize role instances
        this.candidate_role = new Candidate();
        this.follower_role = new Follower();
        this.leader_role = new Leader();
    }

    /**
     * Start processing an incoming connection representing an RPC.
     *
     * @param message the RPC message to process
     * @return the last committed command in the log, or null if no entries
     */
    public String Handle_message(String message) {

        String[] parts = message.split(" ");
        String rpc_type = parts[0];

        if (Role.type.equals("follower")) {
                if (rpc_type.equals("AppendEntries")) {
                    follower_role.append_entries(parts);
                } else if (rpc_type.equals("RequestVote")) {
                    follower_role.request_vote(parts);
                } else {
                    // Handle other message types or ignore
                }
        } else if (Role.type.equals("leader")) {
            if (rpc_type.equals("AppendEntries")) {
                leader_role.append_entries_leader(parts);
            } else if (rpc_type.equals("RequestVote")) {
                // Leaders can also receive RequestVote RPCs and should
                // process them as followers
                follower_role.request_vote(parts);
            } else if (rpc_type.equals("ClientCommand")) {
                // Handle client command
                leader_role.process_client_command(parts);
            } else {
                // Handle other message types or ignore
            }
        } else if (Role.type.equals("candidate")) {
            if (rpc_type.equals("AppendEntries")) {
                follower_role.append_entries(parts);
            } else if (rpc_type.equals("RequestVote")) {
                if (!candidate_role.request_vote(parts)) {
                    follower_role.request_vote(parts);
                }
            } else {
                // Handle other message types or ignore
            }
        }

        // Return last committed command if log is not empty
        if (!Role.log.isEmpty()) {
            return Role.log.get(Role.log.size() - 1).command;
        }
        return null;
    }

    public void send_heartbeat() {
        // Send an empty AppendEntries RPC to all followers to maintain
        // leadership
        follower_role.broadcast(
            "AppendEntries "
            + Role.term + " "
            + Role.id + " "
            + Role.prev_log_index + " "
            + Role.prev_log_term + " "
            + Role.commit_index + " "
            + Role.committed_entries.trim()
        );

        Role.prev_log_index = Role.commit_index;
        Role.prev_log_term = Role.log.get(Role.commit_index).term;
        // Clear committed entries after sending heartbeat
        Role.committed_entries = "";
    }

    public String get_role() {
        return Role.type;
    }

    public void start_election() {
        // Increment term and transition this node into candidate state.
        Role.term++;
        Role.type = "candidate";
        // Vote for self as the current candidate.
        Role.voted_for = Role.id;
        // In a real implementation this would be the node's own identifier
        candidate_role.votes_received = 1; // Vote for self

        // Broadcast RequestVote RPCs to the other cluster nodes.
        candidate_role.broadcast("RequestVote " + 
               Role.term + " " + 
               Role.id + " " + 
               (Role.log.isEmpty() ? -1 : Role.log.size() - 1) + " " + 
               (Role.log.isEmpty() ? -1 : Role.log.get(Role.log.size() - 1).term));
    }
}