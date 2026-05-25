

public class message_info {
    public String message;
    public Comm comm;

    public message_info(String message, Comm comm) {
        this.message = message;
        this.comm = comm;
    }

    public message_info(String message) {
        this.message = message;
        this.comm = null;
    }
}
