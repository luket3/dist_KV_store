/**
 * Follower role for Raft consensus.
 */
public class Follower extends Role {

    public Follower() {
        super();
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

        int candidateTerm = Integer.parseInt(message_parts[1]);
        String candidateId = message_parts[2];
        int lastLogIndex = Integer.parseInt(message_parts[3]);
        int lastLogTerm = Integer.parseInt(message_parts[4]);

        // Check current term and update if necessary
        if (candidateTerm > Role.term) {
            Role.term = candidateTerm;
            Role.type = "follower";
            Role.voted_for = null;
        }

        // Reject if candidate's term is less than current term
        if (candidateTerm < Role.term) {
            return false;
        }
        
        // Check if candidate's log is at least as up-to-date as receiver's log
        int receiverLastLogIndex = Role.log.get_last_idx();
        int receiverLastLogTerm = Role.log.get_last_term();

        boolean sameTerm = (lastLogTerm == receiverLastLogTerm);
        boolean logUpToDate = (lastLogTerm > receiverLastLogTerm)
            || (sameTerm && lastLogIndex >= receiverLastLogIndex);

        // Vote for candidate if we haven't voted or already voted for this
        // candidate, and the candidate's log is up-to-date
        boolean voteGranted = logUpToDate && (
            Role.voted_for == null || Role.voted_for.equals(candidateId)
        );
        if (voteGranted) {
            Role.voted_for = candidateId;
            Role.term = candidateTerm; // Update term to candidate's term
        }

        // Send vote grant status back to the candidate
        send_to_node(
            Role.nodes.get(candidateId),
            "RequestVote " + Role.term + " " + voteGranted
        );

        return voteGranted;
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

        int leaderTerm = Integer.parseInt(message_parts[1]);
        String leaderId = message_parts[2];
        int prevLogIndex = Integer.parseInt(message_parts[3]);
        int prevLogTerm = Integer.parseInt(message_parts[4]);
        int leaderCommit = Integer.parseInt(message_parts[5]);

        // Update term and revert to follower if we see a higher term
        if (leaderTerm > Role.term) {
            Role.term = leaderTerm;
            Role.type = "follower";
            Role.voted_for = null;
        } else if (leaderTerm < Role.term) {
            // Reject if leader's term is less than current term
            return false;
        }

        if (Role.leader == null || !Role.leader.id.equals(leaderId)) {
            // Update leader information if this is a new leader
            Role.leader = Role.nodes.get(leaderId);
            Role.log.clear_uncommitted();
        }

        // Check prevLogIndex and prevLogTerm match
        boolean logMatch = true;
        if (prevLogIndex >= 0) {
            if (prevLogIndex >= Role.log.get_size()) {
                logMatch = false;
            } else if (Role.log.get(prevLogIndex).term != prevLogTerm) {
                logMatch = false;
            }
        }

        if (!logMatch) {
            // Log doesn't match, reject
            return false;
        }

        // Append new entries from message_parts[6] onwards
        String command = "";
        for (int i = 6; i < message_parts.length; i++) {
            if ((message_parts[i].equals("ClientCommand") && !command.isEmpty())) {
                Role.log.append_entry(command.trim(), Role.term);
                command = "";
            } else {
                command += message_parts[i] + " ";
            }

            if (i == message_parts.length - 1 && !command.isEmpty()) {
                Role.log.append_entry(command.trim(), Role.term);
            }
        }

        // Update commitIndex
        Role.log.commit_entries(leaderCommit);

        send_to_node(
            Role.leader,
            "AppendEntries " + Role.term + " true " + Role.log.get_last_idx()
        );
        return true;
    }

    public void hand_to_leader(String message) {
        if (Role.leader != null) {
            send_to_node(Role.leader, "ClientCommand " + message);
        } else {
            // No known leader, could buffer the message or ignore
        }
    }
}
