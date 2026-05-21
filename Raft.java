import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Partial Raft role implementation for handling incoming RPCs.
 *
 * <p>This class models a node's Raft role (follower/candidate/leader) and
 * provides an entry point for processing messages received on a socket.</p>
 */
public class Raft {
    /** Current role type: "follower", "candidate", or "leader". */
    public String type;

    /** In-memory Raft log. */
    LinkedList<log_entry> log;

    /** Communication helper used for the RPC transport. */
    Comm comm;

    /** Cluster nodes known to this Raft instance. */
    List<Node> nodes;

    /**
     * Create a Raft instance bound to a given port (not yet listening).
     *
     * @param port local port the Raft instance will use (reserved for future use)
     * @throws Exception on initialization errors
     */
    Raft(int port) throws Exception {
        this.type = "follower";
    }

    /**
     * Start processing an incoming connection representing an RPC.
     *
     * @param socket connected socket to read an RPC from
     * @return the last committed command in the log (placeholder behaviour)
     */
    public String start(Socket socket) {
        comm = new Comm(socket);

        // create something to tell if message is from leader in comm

        if (type.equals("follower")) {
  
        }
        // if type is leader

        // if type is candidate

        return log.getLast().command;
    }
}
