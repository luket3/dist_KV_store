import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Node {

    public String id; // node identifier
    public String ip; // ip of node
    public String port; // port of node

    // constructs node id, ip and port
    //
    // @param String id: the id of the node
    Node(String id)
    {
        this.id = id; // set id
        try {
            // find nodes ip and port in network.config
            List<String> file_data = Files.readAllLines(Paths.get("network.config"));
            for (String line : file_data) {
                if (line.substring(0, 2).equals(id)) {
                    String[] split = line.split(",");
                    ip = split[1];
                    port = split[2];
                    break;
                }
            }

            if (ip == null || port == null)
                throw new IOException();
        }
        catch (IOException e) {
            System.err.println("failed to construct node from network.config file");
        }
    }

    void print() {
        System.out.println(id + " " + ip + " " + port);
    }
}