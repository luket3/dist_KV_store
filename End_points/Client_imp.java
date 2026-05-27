/*
 * File: Client.java
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Client for the distributed key-value store.
 */

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

/**
 * Client for the distributed key-value store.
 *
 * <p>This class is responsible for loading the cluster configuration into a
 * consistent-hash map, sending queries to the shard responsible for a key,
 * and receiving responses from servers via the {@code Comm} helper.</p>
 */
public class Client_imp {
    /** Map used to determine which shard holds a given key. */
    private Consistent_hash_map map;

    /** Communication helper used to send/receive messages to nodes. */
    private Comm comm;

    /** Raw node lookup by id. */
    private Map<String, Node> nodes;

    private HashSet<String> killed;

    /**
     * Create a new {@code Client} instance and initialize communication and
     * the consistent-hash map.
     *
     * @throws Exception if initialization of underlying components fails
     */
    Client_imp() throws Exception {
        comm = new Comm();
        map = new Consistent_hash_map();
        nodes = new HashMap<>();
        killed = new HashSet<>();
    }

    /**
     * Read the cluster configuration from `network.config`, add each
     * defined node to the consistent-hash map, and perform sample node removals
     * to exercise shard rebalancing.
     *
     * <p>The configuration file is expected to contain one node per line in
     * the format: {@code nodeId,ip,port}.</p>
     *
     * @throws Exception if the configuration file cannot be read or parsed
     */
    public void add_nodes() throws Exception {
        List<String> file_data =
            Files.readAllLines(Paths.get("network.config"));

        for (String line : file_data) {
            String[] split = line.split(",");
            Node n = new Node(
                    split[0],
                    split[1],
                    Integer.parseInt(split[2])
            );
            map.add_node(n);
            nodes.put(n.id, n);
        }

    }

    /**
     * Validate and send a textual query to the shard responsible for the
     * provided key.
     *
     * <p>Supported query formats are:
     * <ul>
     *   <li>{@code Get key}</li>
     *   <li>{@code Delete key}</li>
     *   <li>{@code Put key value}</li>
     * </ul>
     * </p>
     *
     * @param query the query string to send
     * @return {@code true} if the query format is valid (and the send would be
     * attempted), {@code false} for invalid formats
     * @throws Exception on communication errors while attempting to send
     */
    public boolean send_query(String query) throws Exception {
        String[] split = query.split(" ");

        // Handle Kill/Revive targeting a specific node id
        if (split.length == 2 && (split[0].equals("Kill") || split[0].equals("Revive"))) {

            String targetId = split[1];
            if (split[0].equals("Kill"))
                killed.add(targetId);
            else if (split[0].equals("Revive"))
                killed.remove(targetId);

            Node target = nodes.get(targetId);
            if (target == null)
                return false;

            comm.create_socket(target.ip, target.port);
            comm.send_string(query);
            comm.close_socket();

            return false;
            
        }
        // Existing KV operations: Get/Delete key or Put key value
        else if (
            (split.length == 2
             && (split[0].equals("Get") || split[0].equals("Delete")))
            || (split.length == 3 && split[0].equals("Put"))
        ) {
            Node n = map.get_shard(split[1]).get(killed);

            comm.create_socket(n.ip, n.port);
            comm.send_string(query);
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * Read a response string from the currently-open communication socket
     * and close the socket afterwards.
     *
     * @return the response string read from the server
     * @throws Exception on I/O or communication errors
     */
    public String get_response() throws Exception{
        String response = comm.read_string();
        comm.close_socket();
        return response;
    }
}