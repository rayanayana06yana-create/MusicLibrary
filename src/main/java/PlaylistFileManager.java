import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Saves the whole library to a text file and loads it back.
 * One record per line, fields are separated by '|':
 *   U|username|role
 *   I|S|id|title|artist|minutes
 *   I|E|id|title|host|minutes|category|language
 *   P|Personal|name|creator|genre|public|shuffle
 *   P|Collaborative|name|creator|genre|public|maxCollaborators
 *   P|Podcast|name|creator|genre|public|category|language
 *   C|playlistName|username      (collaborator)
 *   L|playlistName|itemId        (media item inside a playlist)
 * The order of records matters: users and items come before playlists.
 */
public class PlaylistFileManager {

    public void write(MusicLibrary library, String fileName) throws IOException {
        if (library == null || fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Library and file name are required.");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8)) {
            for (User user : library.getUsers()) {
                writer.write("U|" + user.getUsername() + "|" + user.getRole() + "\n");
            }

            for (MediaItem item : library.getMediaItems()) {
                if (item instanceof Song) {
                    writer.write("I|S|" + item.getId() + "|" + item.getTitle() + "|"
                            + item.getCreator() + "|" + item.getDurationMinutes() + "\n");
                } else if (item instanceof PodcastEpisode) {
                    PodcastEpisode episode = (PodcastEpisode) item;
                    writer.write("I|E|" + episode.getId() + "|" + episode.getTitle() + "|"
                            + episode.getCreator() + "|" + episode.getDurationMinutes() + "|"
                            + episode.getCategory() + "|" + episode.getLanguage() + "\n");
                }
            }

            for (Playlist playlist : library.getPlaylists()) {
                String common = playlist.getName() + "|" + playlist.getCreator().getUsername()
                        + "|" + playlist.getGenre() + "|" + playlist.isPublic();

                if (playlist instanceof PersonalPlaylist) {
                    PersonalPlaylist personal = (PersonalPlaylist) playlist;
                    writer.write("P|Personal|" + common + "|" + personal.isShuffleEnabled() + "\n");
                } else if (playlist instanceof CollaborativePlaylist) {
                    CollaborativePlaylist group = (CollaborativePlaylist) playlist;
                    writer.write("P|Collaborative|" + common + "|" + group.getMaxCollaborators() + "\n");
                } else if (playlist instanceof PodcastPlaylist) {
                    PodcastPlaylist podcast = (PodcastPlaylist) playlist;
                    writer.write("P|Podcast|" + common + "|" + podcast.getCategory()
                            + "|" + podcast.getLanguage() + "\n");
                }
            }

            for (Playlist playlist : library.getPlaylists()) {
                if (playlist instanceof CollaborativePlaylist) {
                    CollaborativePlaylist group = (CollaborativePlaylist) playlist;
                    for (User user : group.getCollaborators()) {
                        writer.write("C|" + playlist.getName() + "|" + user.getUsername() + "\n");
                    }
                }
                for (MediaItem item : playlist.getMediaItems()) {
                    writer.write("L|" + playlist.getName() + "|" + item.getId() + "\n");
                }
            }
        }
    }

    // Reads the file and builds a new library
    public MusicLibrary read(String fileName) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required.");
        }
        MusicLibrary library = new MusicLibrary();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    processLine(line, library);
                } catch (RuntimeException e) {
                    throw new IOException("Error in file at line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        }
        return library;
    }

    private void processLine(String line, MusicLibrary library) {
        String[] data = line.split("\\|", -1);

        if (data.length == 0) {
            throw new IllegalArgumentException("Empty record.");
        }
        switch (data[0]) {
            case "U":
                requireLength(data, 3);
                library.addUser(new User(data[1], Role.valueOf(data[2])));
                break;

            case "I":
                if (data.length < 3) throw new IllegalArgumentException("Incomplete media record.");
                if ("S".equals(data[1])) requireLength(data, 6);
                else if ("E".equals(data[1])) requireLength(data, 8);
                int id = Integer.parseInt(data[2]);
                int minutes = Integer.parseInt(data[5]);
                if (data[1].equals("S")) {
                    library.addMediaItem(new Song(id, data[3], data[4], minutes));
                } else if (data[1].equals("E")) {
                    library.addMediaItem(new PodcastEpisode(id, data[3], data[4], minutes, data[6], data[7]));
                } else {
                    throw new IllegalArgumentException("Unknown media item type: " + data[1]);
                }
                break;

            case "P":
                if (data.length < 7) throw new IllegalArgumentException("Incomplete playlist record.");
                if ("Podcast".equals(data[1])) requireLength(data, 8);
                else requireLength(data, 7);
                library.addPlaylist(createPlaylist(data, library));
                break;

            case "C":
                requireLength(data, 3);
                library.addCollaborator(data[1], data[2]);
                break;

            case "L":
                requireLength(data, 3);
                library.addItemToPlaylist(data[1], Integer.parseInt(data[2]));
                break;

            default:
                throw new IllegalArgumentException("Unknown record type: " + data[0]);
        }
    }

    private Playlist createPlaylist(String[] data, MusicLibrary library) {
        String type = data[1];
        String name = data[2];
        User creator = library.findUser(data[3]);
        String genre = data[4];
        boolean isPublic = parseBoolean(data[5], "isPublic");

        if (creator == null) {
            throw new IllegalArgumentException("Creator not found: " + data[3]);
        }

        switch (type) {
            case "Personal":
                return new PersonalPlaylist(name, creator, genre, isPublic, parseBoolean(data[6], "shuffleEnabled"));
            case "Collaborative":
                return new CollaborativePlaylist(name, creator, genre, isPublic, Integer.parseInt(data[6]));
            case "Podcast":
                return new PodcastPlaylist(name, creator, genre, isPublic, data[6], data[7]);
            default:
                throw new IllegalArgumentException("Unknown playlist type: " + type);
        }
    }
    private void requireLength(String[] fields, int expected) {
        if (fields.length != expected) {
            throw new IllegalArgumentException("Expected " + expected
                    + " fields, found " + fields.length + ".");
        }
    }

    private boolean parseBoolean(String value, String fieldName) {
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException("Invalid boolean value for " + fieldName + ": " + value);
        }
        return Boolean.parseBoolean(value);
    }

}
