import java.util.ArrayList;

public class Raft_log {
    private ArrayList<Log_entry> committed_log;
    private ArrayList<Log_entry> uncommitted_log;
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
        this.committed_log = new ArrayList<>();
        this.uncommitted_log = new ArrayList<>();
        this.state_machine_in = state_machine_in;
    }

    public void append_entry(String command, int term) {
        String command_str = remove_first_if(command, "ClientCommand");
        Log_entry new_entry = new Log_entry(
                command_str,
                term,
                committed_log.size() + uncommitted_log.size()
        );
        uncommitted_log.add(new_entry);
    }

    public void commit_entries(int up_to_index) {
        while (!uncommitted_log.isEmpty()
                && uncommitted_log.get(0).index <= up_to_index) {
            Log_entry entry_to_commit = uncommitted_log.removeFirst();
            committed_log.add(entry_to_commit);
            state_machine_in.put(entry_to_commit.command);
        }
    }

    public String get_last_committed_command() {
        if (!committed_log.isEmpty()) {
            return committed_log.get(committed_log.size() - 1).command;
        }
        return null; // No committed entries
    }

    public int get_commit_idx() {
        if (!committed_log.isEmpty()) {
            return committed_log.get(committed_log.size() - 1).index;
        }
        return -1; // No committed entries
    }

    public int get_last_idx() {
        return committed_log.size() + uncommitted_log.size() - 1;
    }

    public int get_last_term() {
        if (!uncommitted_log.isEmpty()) {
            return uncommitted_log.get(uncommitted_log.size() - 1).term;
        } else if (!committed_log.isEmpty()) {
            return committed_log.get(committed_log.size() - 1).term;
        }
        return 0; // No entries, so term is 0
    }

    public int get_size() {
        return committed_log.size() + uncommitted_log.size();
    }

    public Log_entry get(int index) {
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

    public void clear_to(int max) {
        if (max == -1) {
            clear_uncommitted();
            return;
        }

        if (max < committed_log.size())
            clear_uncommitted();

        uncommitted_log.subList(max - committed_log.size() + 1,
                                uncommitted_log.size()).clear();
    }

    public String get_as_string(int start, int end) {
        ArrayList<Log_entry> entries = get(start, end);

        if (entries == null)
            return "";

        String as_string = "[";
        for (int i = 0; i < entries.size(); i++) {
            as_string += entries.get(i).command;
            if (i < entries.size() - 1)
                as_string += ",";
        }
        as_string += "]";

        return as_string;
    }

    public ArrayList<Log_entry> get(int start, int end) {
        // Validate
        if (start < 0 || end >= get_size() || end < start)
            return null;

        ArrayList<Log_entry> result = new ArrayList<>();

        int committedSize = committed_log.size();

        // entirely in committed log
        if (end < committedSize) {
            result.addAll(committed_log.subList(start, end + 1));
            return result;
        }

        // entirely in uncommitted log
        if (start >= committedSize) {
            int s = start - committedSize;
            int e = end - committedSize;
            result.addAll(uncommitted_log.subList(s, e + 1));
            return result;
        }

        // Case 3: Spans committed → uncommitted
        result.addAll(committed_log.subList(start, committedSize));
        result.addAll(uncommitted_log.subList(0, end - committedSize + 1));

        return result;
    }

    public void clear_uncommitted() {
        uncommitted_log.clear();
    }
}