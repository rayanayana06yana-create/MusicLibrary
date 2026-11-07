public class PlayRequest implements Comparable<PlayRequest> {

    private final int id;             // business key
    private final User listener;
    private final Playlist playlist;
    private final int priority;       // 1 (low) .. 5 (high); used by the priority queue

    public PlayRequest(int id, User listener, Playlist playlist, int priority) {
        if (listener == null || playlist == null) {
            throw new IllegalArgumentException("Listener and playlist are required.");
        }
        this.id = id;
        this.listener = listener;
        this.playlist = playlist;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public User getListener() {
        return listener;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "Request #" + id + " [" + listener.getUsername() + " -> "
                + playlist.getName() + ", priority " + priority + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlayRequest)) return false;
        return id == ((PlayRequest) obj).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Natural order for PriorityQueue: higher priority first,
    // equal priority -> older request (smaller id) first.
    @Override
    public int compareTo(PlayRequest other) {
        int result = Integer.compare(other.priority, this.priority);
        if (result != 0) {
            return result;
        }
        return Integer.compare(this.id, other.id);
    }
}
