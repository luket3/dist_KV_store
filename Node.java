
public class Node {

    public String id; // node identifier
    public String ip; // ip of node
    public int port; // port of node

    // constructs node id, ip and port
    //
    // @param String id: the id of the node
    // @param String ip: the ip of the node
    // @param String port: the port of the node
    Node(String id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    void print() {
        System.out.println(id + " " + ip + " " + port);
    }
}