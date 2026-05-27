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
 * <p>This class models a node's Raft role
 * (follower/candidate/leader) and provides an entry point for processing
 * messages received via a pipe.</p>
 */
public class Raft implements Runnable {
    Raft_node node;
    Pipe in_pipe;
    Pipe out_pipe;
    private long last_heartbeat_time = -1;
    private static final int HEARTBEAT_INTERVAL_MS = 1000; // 1 second heartbeat interval
    private static final int ELECTION_TIMEOUT_MIN_MS = 2000; // 2 seconds
    private static final int ELECTION_TIMEOUT_MAX_MS = 5000; // 5 seconds
    String node_id;
    boolean alive;

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
        this.alive = true;
    }

    public boolean check_kill_msg(String msg) {
        if (msg == null)
            return false;

        String message_type = msg.split(" ")[0];
        if (message_type.equals("Kill")) {
            alive = false;
            return true;
        }
        else if (message_type.equals("Revive")) {
            alive = true;
            return true;
        }
        return false;
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
                long timeoutMs;
                long now = System.currentTimeMillis();

                if (node.get_role().equals("leader") && alive) {
                    // Leaders send heartbeats on a fixed interval.
                    if (last_heartbeat_time == -1
                            || now - last_heartbeat_time >= HEARTBEAT_INTERVAL_MS) {
                        System.out.println("Node " + node_id
                                + " - Leader sending heartbeats.");
                        last_heartbeat_time = now;
                        node.send_heartbeat();
                    }
                    timeoutMs = HEARTBEAT_INTERVAL_MS;
                } else {
                    // Randomize election timeout between 2 and 5 seconds for
                    // followers and candidates.
                    timeoutMs = ELECTION_TIMEOUT_MIN_MS
                            + (int) (Math.random()
                                    * (ELECTION_TIMEOUT_MAX_MS
                                            - ELECTION_TIMEOUT_MIN_MS));
                }

                String rawMessage = in_pipe.take(Math.max(1, timeoutMs));
                
                if (check_kill_msg(rawMessage) || !alive) {
                    System.out.println("message failed to be delivered to node: " + this.node_id);
                    continue;
                }

                // If we get a message (not null), process it
                if (rawMessage != null) {
                    System.out.println("Node " + node_id
                            + " received a message: " + rawMessage);
                    // Process the message through the Raft node
                    node.Handle_message(rawMessage);
                } else {
                    // Timeout occurred: no message received within the
                    // election timeout

                    if (node.get_role().equals("follower")
                        || node.get_role().equals("candidate")) {
                        // Follower or candidate timeout: start election
                        System.out.println("Node " + node_id
                                + " - Election timeout elapsed. starting"
                                + " election.");
                        node.start_election();
                    }
                    // Leaders do not timeout the same way because they
                    // send heartbeats
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