import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service layer. Keeps every collection and index in sync.
 *
 * Collections used:
 *  - Set<MediaItem>                 registry of media items (no duplicates by id)
 *  - Map<Integer, MediaItem>        primary-key index for media items
 *  - List<Playlist>                 registry of playlists (keeps insertion order)
 *  - Map<String, Playlist>          primary-key index for playlists (by name)
 *  - Map<String, List<Playlist>>    category index: playlists by genre
 *  - Map<String, User>              registry + primary-key index for users
 *  - Map<Role, List<User>>          category index: users by role
 */
public class MusicLibrary {

    private final Set<MediaItem> mediaItems = new HashSet<>();
    private final Map<Integer, MediaItem> itemById = new HashMap<>();

    private final List<Playlist> playlists = new ArrayList<>();
    private final Map<String, Playlist> playlistByName = new HashMap<>();
    private final Map<String, List<Playlist>> playlistsByGenre = new HashMap<>();

    private final Map<String, User> userByName = new HashMap<>();
    private final Map<Role, List<User>> usersByRole = new HashMap<>();

    // ---------- Users ----------

    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        String key = Validation.key(user.getUsername());
        if (userByName.containsKey(key)) {
            throw new IllegalArgumentException("User already exists: " + user.getUsername());
        }
        userByName.put(key, user);
        usersByRole.computeIfAbsent(user.getRole(), r -> new ArrayList<>()).add(user);
    }

    // Removes the user, all playlists created by the user,
    // and the user's membership in other collaborative playlists.
    public void removeUser(String username) {
        User user = userByName.remove(Validation.key(username));
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        List<User> sameRole = usersByRole.get(user.getRole());
        sameRole.remove(user);
        if (sameRole.isEmpty()) {
            usersByRole.remove(user.getRole());
        }

        List<Playlist> owned = new ArrayList<>();
        for (Playlist playlist : playlists) {
            if (playlist.getCreator().equals(user)) {
                owned.add(playlist);
            }
        }
        for (Playlist playlist : owned) {
            removePlaylist(playlist.getName());
        }

        for (Playlist playlist : playlists) {
            if (playlist instanceof CollaborativePlaylist) {
                ((CollaborativePlaylist) playlist).removeCollaborator(user);
            }
        }
    }

    public List<User> getUsers() {
        return new ArrayList<>(userByName.values());
    }

    // Returns null if not found
    public User findUser(String username) {
        return userByName.get(Validation.key(username));
    }

    public List<User> getUsersByRole(Role role) {
        List<User> group = usersByRole.get(role);
        if (group == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(group);
    }

    // ---------- Media items ----------

    public void addMediaItem(MediaItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Media item cannot be null.");
        }
        if (!mediaItems.add(item)) {
            throw new IllegalArgumentException("Media item with id " + item.getId() + " already exists.");
        }
        itemById.put(item.getId(), item);
    }

    // Removes the item from the registry, the index and from every playlist
    public void removeMediaItem(int id) {
        MediaItem item = itemById.remove(id);
        if (item == null) {
            throw new IllegalArgumentException("Media item not found: " + id);
        }
        mediaItems.remove(item);
        for (Playlist playlist : playlists) {
            playlist.removeMediaItem(item);
        }
    }

    // Returns null if not found. O(1)
    public MediaItem findMediaItem(int id) {
        return itemById.get(id);
    }

    // Updates an existing media object by ID and replaces it in every playlist.
    // The replacement must keep the same ID so all indexes remain consistent.
    public void updateMediaItem(MediaItem replacement) {
        if (replacement == null) {
            throw new IllegalArgumentException("Replacement media item cannot be null.");
        }
        MediaItem old = itemById.get(replacement.getId());
        if (old == null) {
            throw new IllegalArgumentException("Media item not found: " + replacement.getId());
        }
        mediaItems.remove(old);
        mediaItems.add(replacement);
        itemById.put(replacement.getId(), replacement);
        for (Playlist playlist : playlists) {
            playlist.replaceMediaItem(replacement);
        }
    }

    public List<MediaItem> findItemsByCreator(String creator) {
        List<MediaItem> result = new ArrayList<>();
        for (MediaItem item : mediaItems) {
            if (item.getCreator().equalsIgnoreCase(Validation.key(creator))) {
                result.add(item);
            }
        }
        return result;
    }

    public List<MediaItem> findItemsShorterThan(int maxMinutes) {
        List<MediaItem> result = new ArrayList<>();
        for (MediaItem item : mediaItems) {
            if (item.getDurationMinutes() <= maxMinutes) {
                result.add(item);
            }
        }
        return result;
    }

    // Natural order (Comparable)
    public List<MediaItem> getItemsSorted() {
        List<MediaItem> result = new ArrayList<>(mediaItems);
        Collections.sort(result);
        return result;
    }

    // Custom order (Comparator)
    public List<MediaItem> getItemsSorted(Comparator<MediaItem> comparator) {
        List<MediaItem> result = new ArrayList<>(mediaItems);
        result.sort(comparator);
        return result;
    }

    public List<MediaItem> getMediaItems() {
        return new ArrayList<>(mediaItems);
    }

    // ---------- Playlists ----------

    public void addPlaylist(Playlist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist cannot be null.");
        }
        if (findUser(playlist.getCreator().getUsername()) == null) {
            throw new IllegalArgumentException(
                    "Creator is not registered: " + playlist.getCreator().getUsername());
        }
        String key = Validation.key(playlist.getName());
        if (playlistByName.containsKey(key)) {
            throw new IllegalArgumentException("Playlist already exists: " + playlist.getName());
        }
        playlists.add(playlist);
        playlistByName.put(key, playlist);
        playlistsByGenre
                .computeIfAbsent(Validation.key(playlist.getGenre()), g -> new ArrayList<>())
                .add(playlist);
    }

    // Updates the public/private flag without affecting collection indexes.
    public void updatePlaylistVisibility(String name, boolean isPublic) {
        getPlaylist(name).setPublic(isPublic);
    }

    public void removePlaylist(String name) {
        Playlist playlist = playlistByName.remove(Validation.key(name));
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist not found: " + name);
        }
        playlists.remove(playlist);
        removeFromGenreIndex(playlist);
    }

    // Updates the genre and moves the playlist to another group in the genre index
    public void updatePlaylistGenre(String name, String newGenre) {
        Playlist playlist = getPlaylist(name);
        String oldKey = Validation.key(playlist.getGenre());

        playlist.setGenre(newGenre);   // throws if newGenre is blank, nothing is changed yet

        removeFromGenreIndex(playlist, oldKey);
        playlistsByGenre
                .computeIfAbsent(Validation.key(newGenre), g -> new ArrayList<>())
                .add(playlist);
    }

    public boolean addItemToPlaylist(String playlistName, int itemId) {
        Playlist playlist = getPlaylist(playlistName);
        MediaItem item = itemById.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Media item not found: " + itemId);
        }
        return playlist.addMediaItem(item);
    }

    public boolean removeItemFromPlaylist(String playlistName, int itemId) {
        Playlist playlist = getPlaylist(playlistName);
        MediaItem item = itemById.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Media item not found: " + itemId);
        }
        return playlist.removeMediaItem(item);
    }

    public boolean addCollaborator(String playlistName, String username) {
        Playlist playlist = getPlaylist(playlistName);
        if (!(playlist instanceof CollaborativePlaylist)) {
            throw new IllegalArgumentException("Playlist is not collaborative: " + playlistName);
        }
        User user = findUser(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        return ((CollaborativePlaylist) playlist).addCollaborator(user);
    }

    // Returns null if not found. O(1)
    public Playlist findPlaylist(String name) {
        return playlistByName.get(Validation.key(name));
    }

    // Returns an empty list if there are no playlists of this genre
    public List<Playlist> getPlaylistsByGenre(String genre) {
        List<Playlist> group = playlistsByGenre.get(Validation.key(genre));
        if (group == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(group);
    }

    public List<Playlist> findPlaylistsLongerThan(int minMinutes) {
        List<Playlist> result = new ArrayList<>();
        for (Playlist playlist : playlists) {
            if (playlist.getDurationMinutes() >= minMinutes) {
                result.add(playlist);
            }
        }
        return result;
    }

    public List<Playlist> getPlaylistsSorted(Comparator<Playlist> comparator) {
        List<Playlist> result = new ArrayList<>(playlists);
        result.sort(comparator);
        return result;
    }

    public List<Playlist> getPlaylists() {
        return new ArrayList<>(playlists);
    }

    // Sorted set of genres that currently have playlists
    public Set<String> getGenres() {
        return new TreeSet<>(playlistsByGenre.keySet());
    }

    public int getUserCount() {
        return userByName.size();
    }

    // ---------- private helpers ----------

    // Playlist must exist, otherwise an exception is thrown
    private Playlist getPlaylist(String name) {
        Playlist playlist = findPlaylist(name);
        if (playlist == null) {
            throw new IllegalArgumentException("Playlist not found: " + name);
        }
        return playlist;
    }

    private void removeFromGenreIndex(Playlist playlist) {
        removeFromGenreIndex(playlist, Validation.key(playlist.getGenre()));
    }

    private void removeFromGenreIndex(Playlist playlist, String genreKey) {
        List<Playlist> group = playlistsByGenre.get(genreKey);
        if (group != null) {
            group.remove(playlist);
            if (group.isEmpty()) {
                playlistsByGenre.remove(genreKey);
            }
        }
    }
}
