import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

public class consistent_hash_map {

    private final TreeMap<Long, Shard> ring; // ring containing node metadata
    private final int virtual_shards = 3; // number of instances of each real node in ring
    private final int min_shard_size = 3;
    private int curr_shard_id = 0;

    private void add_shard(Shard shard) throws Exception {

        if (shard == null)
            shard = new Shard("shard"+curr_shard_id, min_shard_size);

        for (int i = 0; i < virtual_shards; i++)
            ring.put(Hash("shard"+curr_shard_id+i),shard);
        curr_shard_id++;
    }

    private void remove_shard(Shard shard) throws Exception {
        for (int i = 0; i < virtual_shards; i++)
            ring.remove(Hash(shard.id+i));

        List<Node> left_over = shard.get_left();
        for (Node n : left_over) {
            add_node(n);
        }
    }

    // creates empty ring
    public consistent_hash_map() throws Exception {
        ring = new TreeMap<>();
        add_shard(null);
    }

    // add all node n instances to hashmap
    //
    // @param Node n: node which will be added to hash map
    public void add_node(Node n) throws Exception {
        Shard shard = get_shard(n.id);
        shard.add_node(n);

        if (shard.length >= min_shard_size*2)
            add_shard(shard.split("shard"+curr_shard_id));
    }

    // remove all node n instances from hashmap
    //
    // @param String id: id of node to be removed from hash map
    public void remove_node(String id) throws Exception {
        Shard shard = get_shard(id);
        shard.remove_node(id);

        if (shard.length < min_shard_size && ring.size() > 1)
            remove_shard(shard);
    }

    // returns the first n nodes clockwise on ring to Hash(key)
    //
    // @param String key: key to be sharded
    // @param int n: number of nodes to be returned
    // @return Node[]: array of n nodes stored clockwise from Hash(key)
    public Shard get_shard(String key) throws Exception {
        if (ring.size() < 1)
            return null;
        
        SortedMap<Long, Shard> tailMap = ring.tailMap(Hash(key));
        if (tailMap.size() >= 1)
            return tailMap.firstEntry().getValue();
        else
            return ring.firstEntry().getValue();
    }

    // generate MD5 hash
    //
    // @param String s: string to be hashed
    // @return long: MD5 hash of s
    public long Hash(String s) throws Exception {
        long hash = 0;
        MessageDigest md = MessageDigest.getInstance("MD5");

        md.update(s.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();

        for (int i = 0; i < 8; i++)
            hash = (hash << 8) | (long) ((digest[i] & 0xff));
        return hash;
    }

    public void print() {
        for (Map.Entry<Long, Shard> e : ring.entrySet()) {
            System.out.print(e.getValue().id + " ");

            List<Node> left = e.getValue().get_left();
            for (Node n : left) {
                System.out.print(n.id + " ");
            }

            List<Node> right = e.getValue().get_right();
            for (Node n : right) {
                System.out.print(n.id + " ");
            }

            System.out.println();
        }
        System.out.println();
    }
}