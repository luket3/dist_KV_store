/*
 * File: RaftNode.java
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Raft node implementation holding core Raft state.
 */

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a Raft node, storing its role, log, communication channel,
 * and knowledge of the cluster.
 */
public class RaftNode {
    /** Current role type: "follower", "candidate", or "leader". */
    public String type;

    /** In-memory Raft log. */
    public LinkedList<log_entry> log;
    public LinkedList<log_entry> uncommitted_log;

    /** Communication helper used for the RPC transport. */
    public Comm comm;

    /** Cluster nodes known to this Raft instance. */
    public Shard shard;

    

    /**
     * Create a RaftNode instance bound to a given port (not yet listening).
     *
     * @param shard the shard to which this Raft node belongs
     * @throws Exception on initialization errors
     */
    public RaftNode(Shard shard) throws Exception {
        this.type = "follower";
        this.log = new LinkedList<>();
        this.shard = shard;
        // comm will be initialized when a socket is available via start(Socket)
    }

    /**
     * Start processing an incoming connection representing an RPC.
     *
     * @param socket connected socket to read an RPC from
     * @return the last committed command in the log (placeholder behaviour)
     */
    public String start(Socket socket) {
        this.comm = new Comm(socket);

        // create something to tell if message is from leader in comm

        if (type.equals("follower")) {

        }
        // if type is leader

        // if type is candidate

        return log.getLast().command;
    }
}