/**
 * Candidate role for Raft consensus.
 */
public class Candidate extends Role {

    public int votes_received;

    public Candidate() {
        super();
        this.votes_received = 0;
    }

    public boolean request_vote(String[] message_parts) {
        // Handle a RequestVote response while this node is a candidate.
        // Format: RequestVote <term> <voteGranted>
        if (message_parts.length < 3) {
            // Invalid message format
            return true; // Treat as no vote granted
        }
        int voterTerm = Integer.parseInt(message_parts[1]);
        boolean voteGranted = Boolean.parseBoolean(message_parts[2]);

        // Update term and revert to follower if we see a higher term
        if (voterTerm > Role.term) {
            Role.term = voterTerm;
            Role.type = "follower";
            Role.voted_for = null;
            return false;
        }

        // Check if we have won the election
        this.votes_received += voteGranted ? 1 : 0;
        if (this.votes_received > Role.number_of_nodes / 2) {
            Role.type = "leader";
        }
        return true;
    }
}
