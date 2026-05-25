/*
 * File: Raft.java
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Partial Raft role implementation for handling incoming RPCs.
 */

import java.util.Map;

/**
 * Partial Raft role implementation for handling incoming RPCs.
 *
 * <p>This class models a node's Raft role (follower/candidate/leader) and
 * provides an entry point for processing messages received via a pipe.</p>
 */
public class Raft implements Runnable {
    Raft_node node;
    Pipe in_pipe;
    Pipe out_pipe;
    private int TIMEOUT_MS = 3000; // 3 seconds election timeout
    private long last_heartbeat_time = -1;
    private int HEARTBEAT_INTERVAL_MS = 3000; // 1 second heartbeat interval
    String node_id;

    public Raft(
        Pipe in_pipe,
        Pipe out_pipe,
        Map<String, Node> cluster_nodes,
        String node_id
    ) {
        node = new Raft_node(cluster_nodes, node_id, out_pipe);
        this.in_pipe = in_pipe;
        this.out_pipe = out_pipe;
        this.node_id = node_id;
    }

    /**
     * Start listening for incoming messages via the pipe and handle Raft
     * timeouts. If no message is received within the election timeout the
     * node becomes a candidate and an election is started.
     */
    @Override
    public void run() {
        while (true) {
            try {
                // Try to listen for a message with a timeout
                if (node.get_role().equals("leader")) {
                    // Leaders send heartbeats more frequently
                    TIMEOUT_MS = last_heartbeat_time == -1 ? HEARTBEAT_INTERVAL_MS : (int) (HEARTBEAT_INTERVAL_MS - (System.currentTimeMillis() - last_heartbeat_time));
                } else {
                    // Randomize election timeout between 2 and 5 seconds for
                    // followers and candidates
                    TIMEOUT_MS = (int) (Math.random() * 5000) + 5000;
                }
                String rawMessage = in_pipe.take(TIMEOUT_MS);

                // If we get a message (not null), process it
                if (rawMessage != null) {
                    System.out.println("Node " + node_id + " received a message: " + rawMessage);
                    // Process the message through the Raft node
                    node.Handle_message(rawMessage);
                } else {
                    // Timeout occurred: no message received within the
                    // election timeout

                    if (node.get_role().equals("follower")
                        || node.get_role().equals("candidate")) {
                        // Follower or candidate timeout: start election
                        System.out.println("Node " + node_id + " - Election timeout elapsed. starting"
                                           + " election.");
                        node.start_election();
                    }
                    // Leaders do not timeout the same way because they
                    // send heartbeats
                }

                if (node.get_role().equals("leader") && (last_heartbeat_time == -1 || System.currentTimeMillis() - last_heartbeat_time >= HEARTBEAT_INTERVAL_MS)) {
                    // If we're the leader, send heartbeats periodically
                    System.out.println("Node " + node_id + " - Leader sending heartbeats.");
                    last_heartbeat_time = System.currentTimeMillis();
                    node.send_heartbeat();
                }
            } catch (InterruptedException e) {
                // Thread was interrupted, exit gracefully
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Handle other exceptions
                e.printStackTrace();
                // Continue listening
            }
        }
    }
}