/*
 * File: consistent_hash_map.java
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Simple consistent-hash implementation that maps keys to
 * Shard instances.
 */

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

/**
 * Simple consistent-hash implementation that maps keys to {@link Shard}
 * instances.
 *
 * <p>Each real shard is represented by multiple virtual shard entries on the
 * ring (controlled by {@code virtual_shards}). The structure automatically
 * splits and merges shards based on the configured {@code min_shard_size}.</p>
 */
public class Consistent_hash_map {

    /** Ring mapping 64-bit hash values to shard metadata. */
    private final TreeMap<Long, Shard> ring; // ring containing node metadata

    /** Number of virtual shard replicas per real shard on the ring. */
    private final int virtual_shards = 3;
    // number of instances of each real node in ring

    /** Minimum desirable shard size before rebalancing occurs. */
    private final int min_shard_size = 3;

    /** Monotonically-increasing identifier used when creating new shards. */
    private int curr_shard_id = 0;

    /**
     * Add a new shard to the ring. If {@code shard} is {@code null} a new
     * empty {@link Shard} is allocated and placed on the ring.
     */
    private void add_shard(Shard shard) throws Exception {

        if (shard == null)
            shard = new Shard("shard"+curr_shard_id, min_shard_size);

        for (int i = 0; i < virtual_shards; i++)
            ring.put(Hash("shard"+curr_shard_id+i),shard);
        curr_shard_id++;
    }

    /**
     * Remove a shard from the ring and redistribute its leftover nodes.
     */
    private void remove_shard(Shard shard) throws Exception {
        for (int i = 0; i < virtual_shards; i++)
            ring.remove(Hash(shard.id+i));

        List<Node> left_over = shard.get_left();
        for (Node n : left_over) {
            add_node(n);
        }
    }

    /**
     * Create an empty consistent-hash ring and allocate the initial shard.
     */
    public Consistent_hash_map() throws Exception {
        ring = new TreeMap<>();
        add_shard(null);
    }

    /**
     * Add a {@link Node} to its corresponding shard (by node id).
     *
     * @param n node to add
     * @throws Exception on errors during shard splitting or modification
     */
    public void add_node(Node n) throws Exception {
        Shard shard = get_shard(n.id);
        shard.add_node(n);

        if (shard.length >= min_shard_size*2)
            add_shard(shard.split("shard"+curr_shard_id));
    }

    /**
     * Remove a node from the ring by id and redistribute any leftover nodes.
     *
     * @param id identifier of the node to remove
     * @throws Exception on errors during shard merging or modification
     */
    public void remove_node(String id) throws Exception {
        Shard shard = get_shard(id);
        shard.remove_node(id);

        if (shard.length < min_shard_size && ring.size() > 1)
            remove_shard(shard);
    }

    /**
     * Return the shard responsible for the supplied key using consistent hashing.
     *
     * @param key the key to look up
     * @return the {@link Shard} responsible for {@code key}, or {@code null}
     * if the ring is empty
     * @throws Exception on hashing errors
     */
    public Shard get_shard(String key) throws Exception {
        if (ring.size() < 1)
            return null;

        SortedMap<Long, Shard> tailMap = ring.tailMap(Hash(key));
        if (tailMap.size() >= 1)
            return tailMap.firstEntry().getValue();
        else
            return ring.firstEntry().getValue();
    }

    /**
     * Find the shard that contains the specified node id.
     *
     * @param id the node id to search for
     * @return the shard containing the node, or null if not found
     */
    public Shard get_shard_w_node(String id) {
        for (Shard shard : ring.values())
            if (shard.contains(id))
                return shard;

        return null;
    }

    /**
     * Compute a 64-bit value from the MD5 digest of the provided string.
     *
     * @param s input string
     * @return 64-bit hash derived from MD5(s)
     * @throws Exception if the MD5 MessageDigest cannot be obtained
     */
    public long Hash(String s) throws Exception {
        long hash = 0;
        MessageDigest md = MessageDigest.getInstance("MD5");

        md.update(s.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();

        for (int i = 0; i < 8; i++)
            hash = (hash << 8) | (long) ((digest[i] & 0xff));
        return hash;
    }

    /**
     * Print a human-readable representation of the ring and its shards to
     * stdout.
     */
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