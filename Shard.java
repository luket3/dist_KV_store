/*
 * File: Shard.java
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Represents a shard (subset) of nodes stored on the consistent hash ring.
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a shard (subset) of nodes stored on the consistent hash ring.
 *
 * <p>Each {@code Shard} keeps a left and right list of {@link Node}s. New
 * nodes are appended to the left list until the minimum shard size is
 * reached; subsequent nodes go to the right list. Shards can be split into a
 * new shard when the size doubles the minimum.</p>
 */
public class Shard {
    private LinkedList<Node> left;
    private LinkedList<Node> right;
    private int min_shard_size;

    public String id;
    public int length;
    public Node leader;

    /**
     * Internal constructor used when creating a split shard with an initial
     * node list.
     */
    private Shard(String id, int min_shard_size, LinkedList<Node> nodes) {
        this(id,min_shard_size);
        this.left = nodes;
        this.length = nodes.size();
    }

    /**
     * Create an empty shard with the given id and minimum size threshold.
     */
    public Shard(String id, int min_shard_size) {
        this.id = id;
        this.length = 0;
        this.min_shard_size = min_shard_size;

        this.left = new LinkedList<>();
        this.right = new LinkedList<>();
        this.leader = null;
    }

    /**
     * Add a node to this shard. Nodes go to the left list until the shard
     * reaches {@code min_shard_size}, after which nodes are appended to the
     * right list.
     */
    public void add_node(Node n) {
        if (length < min_shard_size)
            left.add(n);
        else
            right.add(n);

        length++;
    }

    /**
     * Remove a node from this shard by id and rebalance left/right lists if
     * necessary.
     */
    public void remove_node(String id) {
        Iterator<Node> it = left.iterator();
        boolean left_side = true;

        while (it.hasNext()) {
            if (it.next().id.equals(id)) {
                it.remove();
                break;
            }

            if (left_side && !it.hasNext()) {
                it = right.iterator();
                left_side = false;
            }
        }
        length--;

        if (left.size() < min_shard_size && right.size() >= 1)
            left.add(right.removeLast());
    }

    /**
     * Split this shard into two when its size equals twice the minimum shard
     * size. The current shard keeps the left list; a new shard is created
     * containing the right list.
     *
     * @param new_id id for the newly created shard
     * @return the newly created {@link Shard}, or {@code null} if splitting is not applicable
     */
    public Shard split(String new_id) {
        if (length != min_shard_size*2)
            return null;

        length = min_shard_size;
        Shard new_shard = new Shard(new_id, min_shard_size, right);
        right = new LinkedList<>();
        return new_shard;
    }

    /** Return nodes currently stored on the left side of the shard. */
    public List<Node> get_left() {
        return left;
    }

    /** Return nodes currently stored on the right side of the shard. */
    public List<Node> get_right() {
        return right;
    }

    /** Return the first node in the left list, or null if empty. */
    public Node get() {
        if (left.isEmpty()) {
            return null;
        }
        return left.getFirst();
    }
}