import java.util.List;
import java.util.Map;

public class LibraryReport {

    private LibraryReport() {
    }

    public static void printStatistics(MusicLibrary library, PlayQueue queue) {
        List<Playlist> playlists = library.getPlaylists();
        List<MediaItem> items = library.getMediaItems();

        System.out.println("Users: " + library.getUserCount()
                + " (listeners: " + library.getUsersByRole(Role.LISTENER).size()
                + ", curators: " + library.getUsersByRole(Role.CURATOR).size() + ")");
        System.out.println("Media items: " + items.size());
        System.out.println("Playlists: " + playlists.size());

        if (playlists.isEmpty()) {
            System.out.println("No playlists yet.");
        } else {
            int totalMinutes = 0;
            Playlist longest = playlists.get(0);
            for (Playlist playlist : playlists) {
                totalMinutes += playlist.getDurationMinutes();
                if (playlist.getDurationMinutes() > longest.getDurationMinutes()) {
                    longest = playlist;
                }
            }
            System.out.println("Total playlist duration: " + totalMinutes + " min");
            System.out.println("Average playlist duration: " + (totalMinutes / playlists.size()) + " min");
            System.out.println("Longest playlist: " + longest.getName()
                    + " (" + longest.getDurationMinutes() + " min)");
        }

        System.out.println("Playlists by genre:");
        for (String genre : library.getGenres()) {
            System.out.println("  " + genre + ": " + library.getPlaylistsByGenre(genre).size());
        }

        System.out.println("Play counts:");
        Map<String, Integer> counts = queue.getPlayCounts();
        if (counts.isEmpty()) {
            System.out.println("  (nothing played yet)");
        }
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Pending requests: " + queue.getPendingCount());
    }
}
