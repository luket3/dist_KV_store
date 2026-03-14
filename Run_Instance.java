
public class Run_Instance {

    private static Node node;

    public static void main(String args[]) {
        node = new Node(args[0]);

        node.print();
    }
}
