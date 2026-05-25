import java.util.LinkedList;

public class Raft_log {
    private LinkedList<log_entry> committed_log;
    private LinkedList<log_entry> uncommitted_log;
    private Pipe state_machine_in;

    private String remove_first_if(String command, String term) {
        int space = command.indexOf(' ');
        if (space != -1) {
            String first = command.substring(0, space);
            if (first.equals(term)) {
                command = command.substring(space + 1);
            }
        }
        return command;
    }

    public Raft_log(Pipe state_machine_in) {
        this.committed_log = new LinkedList<>();
        this.uncommitted_log = new LinkedList<>();
        this.state_machine_in = state_machine_in;
    }

    public void append_entry(String command, int term) {
        System.out.println("node " + Role.id + " appending entry to log: " + command + " (term " + term + ")");
        String command_str = remove_first_if(command, "ClientCommand");
        log_entry new_entry = new log_entry(command_str, term, committed_log.size() + uncommitted_log.size());
        uncommitted_log.add(new_entry);
    }

    public void commit_entries(int up_to_index) {
        System.out.println("node " + Role.id + " committing entries up to index " + up_to_index);
        while (!uncommitted_log.isEmpty() && uncommitted_log.getFirst().index <= up_to_index) {
            log_entry entry_to_commit = uncommitted_log.removeFirst();
            committed_log.add(entry_to_commit);
            state_machine_in.put(entry_to_commit.command);
        }
    }

    public String get_last_committed_command() {
        if (!committed_log.isEmpty()) {
            return committed_log.getLast().command;
        }
        return null; // No committed entries
    }

    public int get_commit_idx() {
        if (!committed_log.isEmpty()) {
            return committed_log.getLast().index;
        }
        return -1; // No committed entries
    }

    public int get_last_idx() {
        return committed_log.size() + uncommitted_log.size() - 1;
    }

    public int get_last_term() {
        if (!uncommitted_log.isEmpty()) {
            return uncommitted_log.getLast().term;
        } else if (!committed_log.isEmpty()) {
            return committed_log.getLast().term;
        }
        return 0; // No entries, so term is 0
    }

    public int get_size() {
        return committed_log.size() + uncommitted_log.size();
    }

    public log_entry get(int index) {
        if (index < committed_log.size()) {
            return committed_log.get(index);
        } else {
            int uncommitted_index = index - committed_log.size();
            if (uncommitted_index < uncommitted_log.size()) {
                return uncommitted_log.get(uncommitted_index);
            }
        }
        return null; // Index out of bounds
    }

    public void clear_uncommitted() {
        uncommitted_log.clear();
    }
}