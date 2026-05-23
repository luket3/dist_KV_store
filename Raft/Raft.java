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
public class Raft {
    RaftNode node;
    Pipe message_pipe;
    private int ELECTION_TIMEOUT_MS = 3000; // 3 seconds election timeout
    private long electionStartTime;

    public Raft(
        Pipe message_pipe,
        Map<String, Node> cluster_nodes,
        String node_id
    ) {
        node = new RaftNode(cluster_nodes, node_id);
        this.message_pipe = message_pipe;
    }

    /**
     * Start listening for incoming messages via the pipe and handle Raft
     * timeouts. If no message is received within the election timeout the
     * node becomes a candidate and an election is started.
     */
    public void start() {
        while (true) {
            try {
                // Try to listen for a message with a timeout
                if (node.get_role().equals("leader")) {
                    // Leaders send heartbeats more frequently
                    ELECTION_TIMEOUT_MS = 1000;
                } else {
                    // Randomize election timeout between 2 and 5 seconds for
                    // followers and candidates
                    ELECTION_TIMEOUT_MS = (int) (Math.random() * 3000) + 2000;
                }
                String rawMessage = message_pipe.take(ELECTION_TIMEOUT_MS);

                // If we get a message (not null), process it
                if (rawMessage != null) {
                    System.out.println("Received a message: " + rawMessage);
                    // Process the message through the Raft node
                    String response = node.Handle_message(rawMessage);
                } else {
                    // Timeout occurred: no message received within the
                    // election timeout

                    if (node.get_role().equals("follower")
                        || node.get_role().equals("candidate")) {
                        // Follower or candidate timeout: start election
                        System.out.println("Election timeout elapsed. starting"
                                           + " election.");
                        node.start_election();
                    }
                    // Leaders do not timeout the same way because they
                    // send heartbeats
                }

                if (node.get_role().equals("leader")) {
                    // If we're the leader, send heartbeats periodically
                    System.out.println("Leader sending heartbeats.");
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