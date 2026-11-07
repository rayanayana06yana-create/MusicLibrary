import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

/**
 * Role workflow:
 *   LISTENER submits requests  ->  queue  ->  CURATOR takes them with poll() and plays.
 *
 * Two queues:
 *   normalQueue   - ArrayDeque, FIFO
 *   priorityQueue - PriorityQueue, ordered by PlayRequest.compareTo (priority, then id)
 * The curator always serves the priority queue first, then the normal queue.
 */
public class PlayQueue {

    private final MusicLibrary library;
    private final Queue<PlayRequest> normalQueue = new ArrayDeque<>();
    private final PriorityQueue<PlayRequest> priorityQueue = new PriorityQueue<>(
            Comparator.comparingInt(PlayRequest::getPriority).reversed()
                    .thenComparingInt(PlayRequest::getId));
    private final Map<String, Integer> playCounts = new TreeMap<>();   // playlist name -> times played
    private int nextRequestId = 1;

    public PlayQueue(MusicLibrary library) {
        if (library == null) {
            throw new IllegalArgumentException("Library cannot be null.");
        }
        this.library = library;
    }

    // Listener -> normal (FIFO) queue
    public PlayRequest submitRequest(String listenerName, String playlistName) {
        PlayRequest request = createRequest(listenerName, playlistName, 1);
        normalQueue.offer(request);
        return request;
    }

    // Listener -> priority queue
    public PlayRequest submitPriorityRequest(String listenerName, String playlistName, int priority) {
        if (priority < 2 || priority > 5) {
            throw new IllegalArgumentException("Priority must be between 2 and 5.");
        }
        PlayRequest request = createRequest(listenerName, playlistName, priority);
        priorityQueue.offer(request);
        return request;
    }

    // Curator takes the next request (poll) and plays the playlist.
    // Returns null if both queues are empty.
    public PlayRequest processNext(String curatorName) {
        User curator = library.findUser(curatorName);
        if (curator == null || curator.getRole() != Role.CURATOR) {
            throw new IllegalArgumentException("Only a registered curator can process requests: " + curatorName);
        }

        while (true) {
            PlayRequest request = priorityQueue.poll();
            if (request == null) {
                request = normalQueue.poll();
            }
            if (request == null) {
                return null;
            }

            // The playlist or the listener could have been deleted while the request was waiting
            Playlist playlist = library.findPlaylist(request.getPlaylist().getName());
            User listener = library.findUser(request.getListener().getUsername());
            if (playlist == null || listener == null) {
                System.out.println("Skipped " + request + " (playlist or listener was removed)");
                continue;
            }

            System.out.println(curator.getUsername() + " processes " + request);
            playlist.play();
            playCounts.merge(playlist.getName(), 1, Integer::sum);
            return request;
        }
    }

    public int getPendingCount() {
        return normalQueue.size() + priorityQueue.size();
    }

    public Map<String, Integer> getPlayCounts() {
        return new TreeMap<>(playCounts);
    }

    public void showQueues() {
        System.out.println("Priority queue (" + priorityQueue.size() + "):");
        // Iterating a PriorityQueue does not give sorted order, so we sort a copy for display
        List<PlayRequest> sorted = new ArrayList<>(priorityQueue);
        sorted.sort(Comparator.comparingInt(PlayRequest::getPriority).reversed()
                .thenComparingInt(PlayRequest::getId));
        for (PlayRequest request : sorted) {
            System.out.println("  " + request);
        }
        System.out.println("Normal queue (" + normalQueue.size() + "):");
        for (PlayRequest request : normalQueue) {
            System.out.println("  " + request);
        }
    }

    private PlayRequest createRequest(String listenerName, String playlistName, int priority) {
        User listener = library.findUser(listenerName);
        if (listener == null || listener.getRole() != Role.LISTENER) {
            throw new IllegalArgumentException("Only a registered listener can submit requests: " + listenerName);
        }
        Playlist playlist = library.findPlaylist(playlistName);
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist not found: " + playlistName);
        }
        if (playlist.getItemCount() == 0) {
            throw new IllegalStateException("Playlist is empty: " + playlistName);
        }
        return new PlayRequest(nextRequestId++, listener, playlist, priority);
    }
}