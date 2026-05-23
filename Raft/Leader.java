/**
 * Leader role for Raft consensus.
 */
public class Leader extends Role {

    public Leader() {
        super();
    }

    public void process_client_command(String[] message_parts) {
        // Handle a client command when this node is the leader.
        // Format: ClientCommand <command>
        if (message_parts.length < 2) {
            // Invalid message format
            return;
        }
        String command = message_parts[1];

        // Append the command to the log as an uncommitted entry
        Role.log.add(new log_entry(command, Role.term, Role.log.size()));
    }

    public void append_entries_leader(String[] message_parts) {
        // Process a follower's AppendEntries response when this node is
        // acting as leader.
        // Format: AppendEntries <term> <success> <matchIndex>

        if (message_parts.length < 4) {
            // Invalid message format
            return;
        }

        int followerTerm = Integer.parseInt(message_parts[1]);
        boolean success = Boolean.parseBoolean(message_parts[2]);
        int matchIndex = Integer.parseInt(message_parts[3]);

        // Update term and revert to follower if we see a higher term
        if (followerTerm > Role.term) {
            Role.term = followerTerm;
            Role.type = "follower";
            Role.voted_for = null;
            return;
        }

        // If AppendEntries was successful, update match index for that follower
        if (success) {
            int prev = Role.votes_received_per_index.getOrDefault(matchIndex, 0);
            Role.votes_received_per_index.put(matchIndex, prev + 1);

            // Check if we have a majority for this index
            for (int i = Role.commit_index + 1; i <= matchIndex; i++) {
                int count = Role.votes_received_per_index.getOrDefault(i, 0);
                if (count > Role.number_of_nodes / 2) {
                    Role.commit_index = i;
                    Role.committed_entries += Role.log.get(i).command + " ";
                } else {
                    break; // No need to check higher indices if this one
                             // lacks majority
                }
            }
        }
    }
}
