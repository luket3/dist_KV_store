import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

public class consistent_hash_map {

    private final TreeMap<Long, Node> ring; // ring containing node metadata
    private final int virtual_nodes = 3; // number of instances of each real node in ring

    // creates empty ring
    public consistent_hash_map() {
        ring = new TreeMap<>();
    }

    // add all node n instances to hashmap
    //
    // @param Node n: node which will be added to hash map
    public void add_node(Node n) throws NoSuchAlgorithmException {
        for (int i = 0; i < virtual_nodes; i++)
            ring.put(Hash(n.id + i), n);
    }

    // remove all node n instances from hashmap
    //
    // @param String id: id of node to be removed from hash map
    public void remove_node(String id) throws NoSuchAlgorithmException {
        for (int i = 0; i < virtual_nodes; i++)
                ring.remove(Hash(id + i));
    }

    // returns the first n nodes clockwise on ring to Hash(key)
    //
    // @param String key: key to be sharded
    // @param int n: number of nodes to be returned
    // @return Node[]: array of n nodes stored clockwise from Hash(key)
    public Node[] get_n_nodes(int n, String key) throws NoSuchAlgorithmException {
        if (ring.size() < n*virtual_nodes)
            return null;
        
        long hash = Hash(key);
        int num_nodes = 0;
        Node[] nodes = new Node[n];
        HashSet<String> visited = new HashSet<>();

        Iterator<Map.Entry<Long, Node>> it = ring.tailMap(hash).entrySet().iterator();
        while (num_nodes < n) {
            if (!it.hasNext())
                it = ring.entrySet().iterator();
            Map.Entry<Long, Node> entry = it.next();

            if (visited.contains(entry.getValue().id))
                continue;

            nodes[num_nodes] = entry.getValue();
            visited.add(entry.getValue().id);
            num_nodes++;
        }

        return nodes;
    }

    // generate MD5 hash
    //
    // @param String s: string to be hashed
    // @return long: MD5 hash of s
    public long Hash(String s) throws NoSuchAlgorithmException {
        long hash = 0;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();

            for (int i = 0; i < 8; i++)
                hash = (hash << 8) | (long) ((digest[i] & 0xff));
        } catch (NoSuchAlgorithmException e) {
            System.err.println("failed to hash " + s);
            throw e;
        }

        return hash;
    }
}