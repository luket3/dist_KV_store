import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client_run_instance {
    
     public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String query = br.readLine();

        Client client = new Client();
        client.add_nodes();

        client.send_query(query);

        String response = client.get_response();

        System.out.println(response);
     }
}
