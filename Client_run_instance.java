import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client_run_instance {
    
   public static void main(String[] args) throws Exception {
      Client client = new Client();
      client.add_nodes();
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      while(true) {
         String query = br.readLine();

         if (!client.send_query(query)) {
            System.out.println("invalid query");
            continue;
         }
         String response = client.get_response();
         System.out.println(response);
      }
   }
}
