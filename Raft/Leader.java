import java.util.Map;
import java.util.HashMap;

/**
 * Leader role for Raft consensus.
 */
public class Leader extends Role {

    private int prev_log_index;
    private int prev_log_term;
    private Map<Integer, Integer> votes_received_per_index;
    private String committed_entries;

    public Leader() {
        super();
        this.prev_log_index = -1;
        this.prev_log_term = 0;
        this.votes_received_per_index = new HashMap<>();
        this.committed_entries = "";
    }

    public void process_client_command(String[] message_parts) {
        // Handle a client command when this node is the leader.
        // Format: ClientCommand <command>
        String command = String.join(" ", message_parts);
        if (!message_parts[0].equals("ClientCommand")) {
            command = "ClientCommand " + command;
        }

        // Append the command to the log as an uncommitted entry
        System.out.println("Leader processing client command: " + command);
        Role.log.append_entry(command, Role.term);
        committed_entries += command + " ";

        send_append_entries();
    }

    public void append_entries(String[] message_parts) {
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
            int prev = votes_received_per_index.getOrDefault(matchIndex, 0);
            votes_received_per_index.put(matchIndex, prev + 1);

            // Check if we have a majority for this index
            for (int i = Role.log.get_commit_idx() + 1; i <= matchIndex; i++) {
                int count = votes_received_per_index.getOrDefault(i, 0);
                if (count > Role.number_of_nodes / 2) {
                    Role.log.commit_entries(i);
                    send_append_entries();
                }
            }
            votes_received_per_index.entrySet().removeIf(entry -> entry.getKey() < Role.log.get_commit_idx());
        }
    }

    public void send_append_entries() {
        // Send an empty AppendEntries RPC to all followers to maintain
        // leadership
        broadcast(
            "AppendEntries "
            + Role.term + " "
            + Role.id + " "
            + prev_log_index + " "
            + prev_log_term + " "
            + Role.log.get_commit_idx() + " "
            + committed_entries.trim()
        );

        prev_log_index = Role.log.get_last_idx();
        prev_log_term = Role.log.get_last_term();
        // Clear committed entries after sending heartbeat
        committed_entries = "";
    }
}
