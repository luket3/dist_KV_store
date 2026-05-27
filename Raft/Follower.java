
/**
 * Follower role for Raft consensus.
 */
public class Follower extends Role {

    public Follower(Raft_state raft_state) {
        super(raft_state);
    }

    /**
     * Process a RequestVote RPC from candidate.
     *
     * @param message_parts the parsed RPC message parts
     * @return true if vote was granted, false otherwise
     */
    public boolean request_vote(String[] message_parts) {
        // Parse RequestVote RPC parameters from message_parts
        // Format: RequestVote <term> <candidateId> <lastLogIndex> <lastLogTerm>
        if (message_parts.length < 5) {
            // Invalid message format
            return false;
        }

        int candidate_term = Integer.parseInt(message_parts[1]);
        String candidate_id = message_parts[2];
        int last_log_index = Integer.parseInt(message_parts[3]);
        int last_log_term = Integer.parseInt(message_parts[4]);

        // Check current term and update if necessary
        if (candidate_term > raft_state.term) {
            raft_state.term = candidate_term;
            raft_state.type = "follower";
            raft_state.voted_for = null;
        }

        // Reject if candidate's term is less than current term
        if (candidate_term < raft_state.term) {
            return false;
        }
        
        // Check if candidate's log is at least as up-to-date as receiver's log
        int receiver_last_log_index = raft_state.log.get_last_idx();
        int receiver_last_log_term = raft_state.log.get_last_term();

        boolean same_term = (last_log_term == receiver_last_log_term);
        boolean log_up_to_date = (last_log_term > receiver_last_log_term)
            || (same_term && last_log_index >= receiver_last_log_index);

        // Vote for candidate if we haven't voted or already voted for this
        // candidate, and the candidate's log is up-to-date
        boolean vote_granted = log_up_to_date && (
            raft_state.voted_for == null || raft_state.voted_for.equals(candidate_id)
        );
        if (vote_granted) {
            raft_state.voted_for = candidate_id;
        }

        // Send vote grant status back to the candidate
        send_to_node(
            raft_state.nodes.get(candidate_id),
            "RequestVoteReply " + raft_state.term + " " + raft_state.id + " " + vote_granted
        );

        return vote_granted;
    }

    /**
     * Process an AppendEntries RPC from leader.
     *
     * @param message_parts the parsed RPC message parts
     * @return true if the RPC was successful, false otherwise
     */
    public boolean append_entries(String[] message_parts) {
        // Parse AppendEntries RPC parameters from message_parts
        // Format: AppendEntries <term> <leaderId> <prevLogIndex>
        // <prevLogTerm> <leaderCommit> [entries...]
        if (message_parts.length < 6) {
            // Invalid message format
            return false;
        }

        int leader_term = Integer.parseInt(message_parts[1]);
        String leader_id = message_parts[2];
        int prev_log_index = Integer.parseInt(message_parts[3]);
        int prev_log_term = Integer.parseInt(message_parts[4]);
        int leader_commit = Integer.parseInt(message_parts[5]);

        // Update term and revert to follower if we see a higher term
        if (leader_term > raft_state.term) {
            raft_state.term = leader_term;
            raft_state.type = "follower";
            raft_state.voted_for = null;
        } else if (leader_term < raft_state.term) {
            // Reject if leader's term is less than current term
            return false;
        }

        if (raft_state.leader == null || !raft_state.leader.id.equals(leader_id)) {
            // Update leader information if this is a new leader
            raft_state.leader = raft_state.nodes.get(leader_id);
            raft_state.log.clear_uncommitted();
        }

        // Check prev_log_index and prev_log_term match
        boolean log_match = true;
        if (prev_log_index >= 0) {
            if (prev_log_index >= raft_state.log.get_size()) {
                log_match = false;
            } else if (raft_state.log.get(prev_log_index).term != prev_log_term) {
                log_match = false;
            }
        }

        if (log_match) {
            // commands are in format:
            // [ClientCommand <insert command>,ClientCommand <insert command>,...]
            if (message_parts.length >= 7 && !message_parts[6].equals("")) {
                String commands = message_parts[6];
                String[] split = commands.substring(1,commands.length() - 1)
                                         .split(",");

                raft_state.log.clear_to(prev_log_index);
                for (String cmd : split)
                    raft_state.log.append_entry(cmd, leader_term);
            }

            // commit upto leadercommit
            raft_state.log.commit_entries(leader_commit);
        }

        send_to_node(
            raft_state.leader,
            "AppendEntriesReply " + raft_state.term  + " "
                                  + raft_state.id + " " 
                                  + log_match + " " 
                                  + raft_state.log.get_last_idx()
        );
        return true;
    }

    public void hand_to_leader(String message) {
        if (raft_state.leader != null) {
            send_to_node(raft_state.leader, message);
        } else {
            // No known leader, could buffer the message or ignore
        }
    }
}
