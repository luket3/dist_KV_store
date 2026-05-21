import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Simple runner that creates a {@code Client} instance and initializes the
 * client-side view of the cluster.
 */
public class Client_run_instance {
    /**
     * Program entry point. Initializes a {@code Client}, loads nodes from
     * configuration, and opens a console reader for interactive queries.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if initialization fails
     */
   public static void main(String[] args) throws Exception {
      Client client = new Client();
      client.add_nodes();
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      /*
      while(true) {
         String query = br.readLine();

         if (!client.send_query(query)) {
            System.out.println("invalid query");
            continue;
         }
         String response = client.get_response();
         System.out.println(response);
      }
         */
   }
}
