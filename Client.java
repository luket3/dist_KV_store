import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Client extends Machine {
    public static consistent_hash_map map; // map used to shard keys

    // adds nodes to consistent hash map
    public static void add_nodes() throws NoSuchAlgorithmException {
        try {
            List<String> file_data = Files.readAllLines(Paths.get("network.config"));
            for (String line : file_data) {
                String[] split = line.split(",");
                map.add_node(new Node(split[0], split[1], Integer.parseInt(split[2])));
            }
        } catch (IOException e) {
            System.err.println("failed to add nodes to hash map");
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        map = new consistent_hash_map();
        Client.add_nodes();

        Node[] nodes =  map.get_n_nodes(8,"key12");
        for (Node node : nodes) {
            System.out.print(node.id + ' ');
        }
        System.out.println();

        nodes =  map.get_n_nodes(8,"key122");
        for (Node node : nodes) {
            System.out.print(node.id + ' ');
        }
        System.out.println();

        nodes =  map.get_n_nodes(8,"key1252");
        for (Node node : nodes) {
            System.out.print(node.id + ' ');
        }
        System.out.println();
        
    }
}
