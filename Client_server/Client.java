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

/**
 * Client for the distributed key-value store.
 *
 * <p>This class is responsible for loading the cluster configuration into a
 * consistent-hash map, sending queries to the shard responsible for a key,
 * and receiving responses from servers via the {@code Comm} helper.</p>
 */
public class Client {
    /** Map used to determine which shard holds a given key. */
    private consistent_hash_map map;

    /** Communication helper used to send/receive messages to nodes. */
    private Comm comm;

    /**
     * Create a new {@code Client} instance and initialize communication and
     * the consistent-hash map.
     *
     * @throws Exception if initialization of underlying components fails
     */
    Client() throws Exception {
        comm = new Comm();
        map = new consistent_hash_map();
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
            map.add_node(
                new Node(
                    split[0],
                    split[1],
                    Integer.parseInt(split[2])
                )
            );
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

        if (
            (split.length == 2
             && (split[0].equals("Get") || split[0].equals("Delete")))
            || (split.length == 3 && split[0].equals("Put"))
        ) {
            Node n = map.get_shard(split[1]).get();

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