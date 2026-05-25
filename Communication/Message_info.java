

public class Message_info {
    public String message;
    public Comm comm;

    public Message_info(String message, Comm comm) {
        this.message = message;
        this.comm = comm;
    }

    public Message_info(String message) {
        this.message = message;
        this.comm = null;
    }
}
