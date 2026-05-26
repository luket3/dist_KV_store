import java.util.HashMap;

/**
 * Leader role for Raft consensus.
 */
public class Leader extends Role {

    private HashMap<String, Integer> match_index;
    private HashMap<String, Integer> next_index;

    public Leader(Raft_state raft_state) {
        super(raft_state);
        this.match_index = new HashMap<>();
        this.next_index = new HashMap<>();

        for (String node_id : raft_state.nodes.keySet()) {
            this.match_index.put(node_id, -1);
            this.next_index.put(node_id, raft_state.log.get_last_idx() + 1);
        }

    }

    public void process_client_command(String command) {
        // Handle a client command when this node is the leader.
        // Format: ClientCommand <command>

        // Append the command to the log as an uncommitted entry
        System.out.println("Leader processing client command: " + command);
        raft_state.log.append_entry(command, raft_state.term);

        broadcast_append_entries();
    }

    public void append_entries(String[] message_parts) {
        // Process a follower's AppendEntries response when this node is
        // acting as leader.
        // Format: AppendEntries <term> <senderID> <success> <matchIndex>

        if (message_parts.length < 4) {
            // Invalid message format
            return;
        }

        int follower_term = Integer.parseInt(message_parts[1]);
        String sender_id = message_parts[2];
        boolean success = Boolean.parseBoolean(message_parts[3]);
        int sndr_match_idx = Integer.parseInt(message_parts[4]);

        // Update term and revert to follower if we see a higher term
        if (follower_term > raft_state.term) {
            raft_state.term = follower_term;
            raft_state.type = "follower";
            raft_state.voted_for = null;
            return;
        }

        // If AppendEntries was successful, update match index for that follower
        if (success) {

            // register follower as having entries up to sndr_match_idx
            match_index.put(sender_id, Math.max(match_index.get(sender_id), sndr_match_idx));
            next_index.put(sender_id, match_index.get(sender_id) + 1);

            // commit such that majority nodes have log entry
            for (int i = raft_state.log.get_last_idx(); i > raft_state.log.get_commit_idx(); i--) {
                
                long count = 0;
                for (int value : match_index.values()) {
                    if (value >= i)
                        count++;
                }

                if (count > raft_state.number_of_nodes / 2) {
                    raft_state.log.commit_entries(i);
                    broadcast_append_entries();
                    break;
                }
            }
        }
        else {
            next_index.put(sender_id, next_index.get(sender_id) - 1);
            send_append_entries(raft_state.nodes.get(sender_id));
        }
    }

    public void broadcast_append_entries() {
        System.out.println("leader node: " + raft_state.id + 
                           " broadcasting append entries message");

        for (Node node : raft_state.nodes.values()) {
            if (!node.id.equals(raft_state.id))
                send_append_entries(node);
        }
    }

    public void send_append_entries(Node node) {
        // Send an empty AppendEntries RPC to all followers to maintain
        // leadership

        // update prev_log_index and prev_log_term
        int prev_log_index = next_index.get(node.id) - 1;
        int prev_log_term = (prev_log_index >= 0) ? 
                                raft_state.log.get(prev_log_index).term : 0;

        send_to_node(node, 
            "AppendEntries "
            + raft_state.term + " "
            + raft_state.id + " "
            + prev_log_index + " "
            + prev_log_term + " "
            + raft_state.log.get_commit_idx() + " "
            + raft_state.log.get_as_string(next_index.get(node.id), 
                                            raft_state.log.get_last_idx())
        );
    }
}
