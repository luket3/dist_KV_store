import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Shard {
    private LinkedList<Node> left;
    private LinkedList<Node> right;
    private int min_shard_size;

    public String id;
    public int length;

    private Shard(String id, int min_shard_size, LinkedList<Node> nodes) {
        this(id,min_shard_size);
        this.left = nodes;
        this.length = nodes.size();
    }

    public Shard(String id, int min_shard_size) {
        this.id = id;
        this.length = 0;
        this.min_shard_size = min_shard_size;

        this.left = new LinkedList<>();
        this.right = new LinkedList<>();
    }

    public void add_node(Node n) {
        if (length < min_shard_size)
            left.add(n);
        else
            right.add(n);

        length++;
    }

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

    public Shard split(String new_id) {
        if (length != min_shard_size*2)
            return null;

        length = min_shard_size;
        Shard new_shard = new Shard(new_id, min_shard_size, right);
        right = new LinkedList<>();
        return new_shard;
    }

    public List<Node> get_left() {
        return left;
    }

    public List<Node> get_right() {
        return right;
    }
}