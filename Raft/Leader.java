import java.util.*;

/**
 * Leader role for Raft consensus.
 */
public class Leader extends Role {

    public Leader(Raft_state raft_state) {
        super(raft_state);
    }

    public void process_client_command(String command) {
        // Handle a client command when this node is the leader.
        // Format: ClientCommand <command>

        // Append the command to the log as an uncommitted entry
        System.out.println("Leader processing client command: " + command);
        raft_state.log.append_entry(command, raft_state.term);
        // Update own match_index to reflect the new entry
        raft_state.match_index.put(raft_state.id, raft_state.log.get_last_idx());

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
            raft_state.match_index.put(sender_id, Math.max(raft_state.match_index.get(sender_id), sndr_match_idx));
            raft_state.next_index.put(sender_id, raft_state.match_index.get(sender_id) + 1);

            // commit such that majority nodes have log entry
            for (int i = raft_state.log.get_last_idx(); i > raft_state.log.get_commit_idx(); i--) {

                long count = 0;
                for (int value : raft_state.match_index.values()) {
                    if (value >= i)
                        count++;
                }

                if (count > raft_state.number_of_nodes / 2 
                        && raft_state.log.get(i).term == raft_state.term) {
                    raft_state.log.commit_entries(i);
                    broadcast_append_entries();
                    break;
                }
            }
        }
        else {
            raft_state.next_index.put(sender_id, raft_state.next_index.get(sender_id) - 1);
            if (raft_state.next_index.get(sender_id) < 0)
                raft_state.next_index.put(sender_id, 0);

            // check if raft log needs to be truncated
            // Compute a truncate point based on majority of followers' next_index.
            // If a majority of nodes have next_index <= K, then their last
            // stored index is <= K-1; leader should truncate uncommitted
            // entries beyond K-1 to match the cluster.
            ArrayList<Integer> nextIndices = new ArrayList<>();
            for (Integer v : raft_state.next_index.values())
                nextIndices.add(v);
            Collections.sort(nextIndices);

            int majorityPos = raft_state.number_of_nodes / 2; // 0-based
            if (majorityPos < nextIndices.size()) {
                int K = nextIndices.get(majorityPos);
                int truncateTo = K - 1;

                if (truncateTo < raft_state.log.get_last_idx()) {
                    System.out.println("Leader: truncating uncommitted entries to index " + truncateTo);
                    raft_state.log.clear_to(truncateTo);

                    // Ensure match_index and next_index are consistent with truncated log
                    int lastIdx = raft_state.log.get_last_idx();
                    for (String nid : raft_state.match_index.keySet()) {
                        int mi = raft_state.match_index.get(nid);
                        if (mi > lastIdx)
                            raft_state.match_index.put(nid, lastIdx);
                        int ni = raft_state.next_index.get(nid);
                        if (ni > lastIdx + 1)
                            raft_state.next_index.put(nid, lastIdx + 1);
                    }
                }
            }

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
        int prev_log_index = raft_state.next_index.get(node.id) - 1;
        int prev_log_term = (prev_log_index >= 0) ?
                                raft_state.log.get(prev_log_index).term : 0;

        send_to_node(node,
            "AppendEntries "
            + raft_state.term + " "
            + raft_state.id + " "
            + prev_log_index + " "
            + prev_log_term + " "
            + raft_state.log.get_commit_idx() + " "
            + raft_state.log.get_as_string(raft_state.next_index.get(node.id),
                                            raft_state.log.get_last_idx())
        );
    }
}
