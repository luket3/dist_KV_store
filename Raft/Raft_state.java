import java.util.HashMap;
import java.util.Map;

public class Raft_state {

    public int term;
    public String id;
    public Map<String, Node> nodes;
    public Raft_log log;
    public Node leader;
    public String voted_for;
    public int number_of_nodes;
    public String type;
    public HashMap<String, Integer> match_index;
    public HashMap<String, Integer> next_index;

    public Raft_state(
            Map<String, Node> cluster_nodes,
            String node_id,
            Pipe state_machine_in
    ) {
        this.nodes = cluster_nodes;
        this.id = node_id;
        this.log = new Raft_log(state_machine_in);
        this.term = 0;
        this.voted_for = null;
        this.leader = null;
        this.number_of_nodes = cluster_nodes.size();
        this.type = "follower";
    }

    /**
     * Initializes the match_index and next_index for leader state.
     * Should be called when a node becomes leader.
     */
    public void initializeLeaderState() {
        this.match_index = new HashMap<>();
        this.next_index = new HashMap<>();

        for (String node_id : this.nodes.keySet()) {
            this.match_index.put(node_id, -1);
            this.next_index.put(node_id, this.log.get_last_idx() + 1);
        }
    }
}
