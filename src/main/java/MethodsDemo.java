
import java.util.List;

public class MethodsDemo {

    public static void run(MusicLibrary library, PlayQueue queue) {

        System.out.println("\n====================================");
        System.out.println("       METHODS DEMONSTRATION");
        System.out.println("====================================");

        // 1. Find media item by ID
        System.out.println("\n1. FIND MEDIA ITEM BY ID");
        System.out.println(library.findMediaItem(1));

        // 2. Find user
        System.out.println("\n2. FIND USER");
        System.out.println(library.findUser("listener"));

        // 3. Find playlist
        System.out.println("\n3. FIND PLAYLIST");
        System.out.println(library.findPlaylist("My Favorites"));

        // 4. Find items by creator
        System.out.println("\n4. FIND ITEMS BY CREATOR");
        List<MediaItem> creatorItems =
                library.findItemsByCreator("Ed Sheeran");
        creatorItems.forEach(System.out::println);

        // 5. Find short media items
        System.out.println("\n5. FIND ITEMS SHORTER THAN 5 MINUTES");
        List<MediaItem> shortItems =
                library.findItemsShorterThan(5);
        shortItems.forEach(System.out::println);

        // 6. Show all media items
        System.out.println("\n6. SHOW ALL MEDIA ITEMS");
        library.getMediaItems().forEach(System.out::println);

        // 7. Show all users
        System.out.println("\n7. SHOW ALL USERS");
        library.getUsers().forEach(System.out::println);

        // 8. Show all playlists
        System.out.println("\n8. SHOW ALL PLAYLISTS");
        library.getPlaylists().forEach(System.out::println);

        // 9. Sort media by natural order
        System.out.println("\n9. SORT MEDIA ITEMS");
        library.getItemsSorted().forEach(System.out::println);

        // 10. Sort by duration
        System.out.println("\n10. SORT BY DURATION");
        library.getItemsSorted(
                MediaItemComparators.BY_DURATION
        ).forEach(System.out::println);

        // 11. Find playlists by genre
        System.out.println("\n11. FIND PLAYLISTS BY GENRE");
        library.getPlaylistsByGenre("Pop")
                .forEach(System.out::println);

        // 12. Show all genres
        System.out.println("\n12. SHOW ALL GENRES");
        System.out.println(library.getGenres());

        // 13. Show users by role
        System.out.println("\n13. SHOW LISTENERS");
        library.getUsersByRole(Role.LISTENER)
                .forEach(System.out::println);

        // 14. Show user count
        System.out.println("\n14. USER COUNT");
        System.out.println(library.getUserCount());

        // 15. Show queue
        System.out.println("\n15. SHOW QUEUES");
        queue.showQueues();

        // 16. Show pending requests
        System.out.println("\n16. PENDING REQUESTS");
        System.out.println(queue.getPendingCount());

        // 17. Show playback statistics
        System.out.println("\n17. PLAYBACK STATISTICS");
        System.out.println(queue.getPlayCounts());

        System.out.println("\n====================================");
        System.out.println("       DEMONSTRATION FINISHED");
        System.out.println("====================================");

    }
}
