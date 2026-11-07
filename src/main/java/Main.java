import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive console application for the music library.
 * Demo accounts: listener / curator
 */
public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String DATA_FILE = "library.txt";

    private static MusicLibrary library;
    private static PlayQueue queue;
    private static final PlaylistFileManager fileManager = new PlaylistFileManager();

    public static void main(String[] args) {
        library = createDemoLibrary();
        queue = new PlayQueue(library);

        System.out.println("====================================");
        System.out.println("       MUSIC LIBRARY SYSTEM");
        System.out.println("====================================");
        System.out.println("Demo accounts: listener / curator");

        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Login as listener");
            System.out.println("2. Login as curator");
            System.out.println("3. Show music catalog");
            System.out.println("4. Save library to file");
            System.out.println("5. Load library from file");
            System.out.println("6. Show methods demonstration");
            System.out.println("0. Exit");

            int choice = readInt("Choose: ");
            switch (choice) {
                case 1 -> listenerLogin();
                case 2 -> curatorLogin();
                case 3 -> showCatalog();
                case 4 -> saveLibrary();
                case 5 -> loadLibrary();
                case 6 -> MethodsDemo.run(library, queue);
                case 0 -> running = false;
                default -> System.out.println("Unknown menu option. Try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static MusicLibrary createDemoLibrary() {
        MusicLibrary result = new MusicLibrary();
        User listener = new User("listener", Role.LISTENER);
        User secondListener = new User("alex", Role.LISTENER);
        User curator = new User("curator", Role.CURATOR);
        result.addUser(listener);
        result.addUser(secondListener);
        result.addUser(curator);

        result.addMediaItem(new Song(1, "Blinding Lights", "The Weeknd", 4));
        result.addMediaItem(new Song(2, "Perfect", "Ed Sheeran", 5));
        result.addMediaItem(new Song(3, "Lovely", "Billie Eilish", 4));
        result.addMediaItem(new PodcastEpisode(4, "Java Basics", "Tech Studio", 25,
                "Programming", "English"));

        result.addPlaylist(new PersonalPlaylist("My Favorites", listener,
                "Pop", true, false));
        result.addPlaylist(new CollaborativePlaylist("Road Trip", secondListener,
                "Mixed", true, 5));
        result.addPlaylist(new PodcastPlaylist("Tech Talks", listener,
                "Education", true, "Programming", "English"));

        result.addItemToPlaylist("My Favorites", 1);
        result.addItemToPlaylist("My Favorites", 2);
        result.addItemToPlaylist("Road Trip", 2);
        result.addItemToPlaylist("Road Trip", 3);
        result.addItemToPlaylist("Tech Talks", 4);
        return result;
    }

    private static void listenerLogin() {
        User user = loginAs(Role.LISTENER);
        if (user == null) return;

        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- LISTENER MENU: " + user.getUsername() + " ---");
            System.out.println("1. View music catalog");
            System.out.println("2. View playlists");
            System.out.println("3. Send play request");
            System.out.println("4. Send priority request");
            System.out.println("0. Back");
            switch (readInt("Choose: ")) {
                case 1 -> showCatalog();
                case 2 -> showPlaylists();
                case 3 -> submitRequest(user, false);
                case 4 -> submitRequest(user, true);
                case 0 -> inMenu = false;
                default -> System.out.println("Unknown menu option.");
            }
        }
    }

    private static void curatorLogin() {
        User user = loginAs(Role.CURATOR);
        if (user == null) return;

        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- CURATOR MENU: " + user.getUsername() + " ---");
            System.out.println("1. Show pending requests");
            System.out.println("2. Process next request");
            System.out.println("3. Show catalog and playlists");
            System.out.println("4. Show library statistics");
            System.out.println("5. Save library");
            System.out.println("6. Load library");
            System.out.println("0. Back");
            switch (readInt("Choose: ")) {
                case 1 -> queue.showQueues();
                case 2 -> {
                    try {
                        PlayRequest processed = queue.processNext(user.getUsername());
                        if (processed == null) System.out.println("There are no pending requests.");
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        System.out.println("Cannot process request: " + e.getMessage());
                    }
                }
                case 3 -> { showCatalog(); showPlaylists(); }
                case 4 -> LibraryReport.printStatistics(library, queue);
                case 5 -> saveLibrary();
                case 6 -> loadLibrary();
                case 0 -> inMenu = false;
                default -> System.out.println("Unknown menu option.");
            }
        }
    }

    private static User loginAs(Role requiredRole) {
        System.out.print("Enter username: ");
        String username = SCANNER.nextLine().trim();
        User user = library.findUser(username);
        if (user == null) {
            System.out.println("User not found.");
            return null;
        }
        if (user.getRole() != requiredRole) {
            System.out.println("Access denied. Your role is " + user.getRole() + ".");
            return null;
        }
        System.out.println("Welcome, " + user.getUsername() + "!");
        return user;
    }

    private static void submitRequest(User user, boolean priority) {
        showPlaylists();
        System.out.print("Enter playlist name: ");
        String playlistName = SCANNER.nextLine().trim();
        try {
            if (priority) {
                int level = readInt("Priority (1-5): ");
                queue.submitPriorityRequest(user.getUsername(), playlistName, level);
            } else {
                queue.submitRequest(user.getUsername(), playlistName);
            }
            System.out.println("Request added to the queue.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Could not add request: " + e.getMessage());
        }
    }

    private static void showCatalog() {
        System.out.println("\n--- MUSIC CATALOG ---");
        List<MediaItem> items = library.getItemsSorted();
        if (items.isEmpty()) {
            System.out.println("Catalog is empty.");
            return;
        }
        for (MediaItem item : items) System.out.println(item);
    }

    private static void showPlaylists() {
        System.out.println("\n--- PLAYLISTS ---");
        List<Playlist> playlists = library.getPlaylists();
        if (playlists.isEmpty()) {
            System.out.println("There are no playlists.");
            return;
        }
        for (Playlist playlist : playlists) {
            System.out.println("- " + playlist.getName() + " [" + playlist.getPlaylistType()
                    + "] | genre: " + playlist.getGenre()
                    + " | tracks: " + playlist.getItemCount());
        }
    }

    private static void saveLibrary() {
        try {
            fileManager.write(library, DATA_FILE);
            System.out.println("Library saved to " + DATA_FILE);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    private static void loadLibrary() {
        try {
            library = fileManager.read(DATA_FILE);
            queue = new PlayQueue(library);
            System.out.println("Library loaded from " + DATA_FILE + ". Queue has been reset.");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Load error: " + e.getMessage());
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

}
