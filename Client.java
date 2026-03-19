import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Client {
    private consistent_hash_map map; // map used to shard keys
    private Comm comm;


    Client() {
        comm = new Comm();
        map = new consistent_hash_map();
    }

    // adds nodes to consistent hash map
    public void add_nodes() throws Exception {
        List<String> file_data = Files.readAllLines(Paths.get("network.config"));

        for (String line : file_data) {
            String[] split = line.split(",");
            map.add_node(new Node(split[0], split[1], Integer.parseInt(split[2])));
        }
    }

    public boolean send_query(String query) throws Exception {
        String[] split = query.split(" ");

        if ((split.length == 2 && (split[0].equals("Get") || split[0].equals("Delete"))) ||
        (split.length == 3 && split[0].equals("Put"))) {
            Node[] n = map.get_n_nodes(1,split[1]);
            comm.create_socket(n[0].ip, n[0].port);
            comm.send_string(query);
        }
        else {
            return false;
        }
        return true;
    }

    public String get_response() throws Exception{
        String response = comm.read_string();
        comm.close_socket();
        return response;
    }
}
