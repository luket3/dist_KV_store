import java.util.SortedMap;
import java.util.TreeMap;

public class client_side_map {

    private final TreeMap<Long, Node> ring; // ring containing node metadata
    private final int virtual_nodes = 3; // number of instances of each real node in ring

    // creates client side hash map
    public client_side_map() {
        ring = new TreeMap<>();
    }

    // add node to hash map
    //
    // @param Node n: node which will be added to hash map
    public void add_node(Node n) {
        // add all node instances to ring
        for (int i = 0; i < virtual_nodes; i++)
            ring.put(Hash(n.id + i), n);
    }

    // remove node from hash map
    //
    // @param String id: id of node to be removed from hash map
    public void remove_node(String id) {
        // remove all node instances from ring
        for (int i = 0; i < virtual_nodes; i++)
                ring.remove(Hash(id + i));
    }

    // gets node with id:id in hash map
    //
    // @param String id: id of node to be gotten from hash map
    public Node get_node(String id) {
        if (ring.isEmpty())
            return null;

        // return the first node clockwise on ring to Hash(id)
        SortedMap<Long, Node> greater_values = ring.tailMap(Hash(id));
        if (greater_values.isEmpty())
            return ring.firstEntry().getValue();
        else
            return greater_values.firstEntry().getValue();
    }

    // generate MD5 hash
    //
    // @param String s: string to be hashed
    // @return long: MD5 hash of s
    public long Hash(String s) {
        return 0;
    }
}
