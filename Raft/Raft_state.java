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
}
