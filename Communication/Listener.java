/*
 * File: Request_handler.java
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Simple blocking request handler that wraps a ServerSocket.
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Simple blocking request handler that wraps a {@link ServerSocket}.
 */
public class Listener {
    private ServerSocket server_socket;

    /**
     * Create and bind a listening socket on the given port.
     *
     * @param port local TCP port to listen on
     * @throws Exception on socket creation errors
     */
    public void create_socket(int port) throws Exception {
        server_socket = new ServerSocket(port);
    }

    /**
     * Close the listening socket.
     *
     * @throws Exception on socket close errors
     */
    public void close_socket() throws Exception {
        server_socket.close();
    }

    /**
     * Block until an incoming connection is accepted and return the socket.
     *
     * @return accepted {@link Socket}
     * @throws Exception on accept failures
     */
    public Socket listen_for_connection() throws Exception {
        Socket socket = server_socket.accept();
        return socket;
    }

    /**
     * Listen for an incoming connection with a specified timeout.
     * Returns null if timeout occurs, otherwise returns the accepted socket.
     *
     * @param timeoutMillis timeout in milliseconds
     * @return accepted Socket or null if timeout
     * @throws Exception on accept failures (other than timeout)
     */
    public Socket listen_for_connection_with_timeout(
        int timeoutMillis
    ) throws Exception {
        // Set timeout on the ServerSocket
        server_socket.setSoTimeout(timeoutMillis);
        try {
            Socket socket = server_socket.accept();
            // Reset timeout to 0 (no timeout) for subsequent calls if needed
            // server_socket.setSoTimeout(0);
            return socket;
        } catch (SocketTimeoutException e) {
            // Timeout occurred
            return null;
        } finally {
            // Reset to no timeout for normal blocking calls
            server_socket.setSoTimeout(0);
        }
    }
}