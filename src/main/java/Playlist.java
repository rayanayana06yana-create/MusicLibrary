import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

public abstract class Playlist implements Playable {

    private final String name;      // business key (case-insensitive)
    private final User creator;     // 1:N  User -> Playlists
    private String genre;
    private boolean isPublic;
    private final List<MediaItem> mediaItems = new ArrayList<>();   // N:M with MediaItem

    public Playlist(String name, User creator, String genre, boolean isPublic) {
        this.name = Validation.requireText(name, "Name");
        if (creator == null) {
            throw new IllegalArgumentException("Creator cannot be null.");
        }
        this.creator = creator;
        this.genre = Validation.requireText(genre, "Genre");
        this.isPublic = isPublic;
    }

    public String getName() {
        return name;
    }

    public User getCreator() {
        return creator;
    }

    public String getGenre() {
        return genre;
    }

    void setGenre(String genre) {
        this.genre = Validation.requireText(genre, "Genre");
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    // Returns false if the item is already in the playlist
    public boolean addMediaItem(MediaItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Media item cannot be null.");
        }
        if (mediaItems.contains(item)) {
            return false;
        }
        return mediaItems.add(item);
    }

    // Returns false if the item was not in the playlist
    public boolean removeMediaItem(MediaItem item) {
        return mediaItems.remove(item);
    }

    // Replaces an item with the same ID; used by MusicLibrary to keep N:M links synchronized.
    boolean replaceMediaItem(MediaItem replacement) {
        if (replacement == null) {
            throw new IllegalArgumentException("Replacement item cannot be null.");
        }
        for (int i = 0; i < mediaItems.size(); i++) {
            if (mediaItems.get(i).getId() == replacement.getId()) {
                mediaItems.set(i, replacement);
                return true;
            }
        }
        return false;
    }

    // A copy is returned so that outsiders cannot change the internal list
    public List<MediaItem> getMediaItems() {
        return new ArrayList<>(mediaItems);
    }

    public int getItemCount() {
        return mediaItems.size();
    }

    public int getDurationMinutes() {
        int total = 0;
        for (MediaItem item : mediaItems) {
            total += item.getDurationMinutes();
        }
        return total;
    }

    public abstract String getPlaylistType();

    public abstract int calculateRecommendedListeningTime();

    @Override
    public String toString() {
        return name + " [" + getPlaylistType() + ", " + genre + ", "
                + mediaItems.size() + " items, " + getDurationMinutes() + " min]";
    }

    // equals/hashCode use only the business key (name, ignoring case)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Playlist)) return false;
        Playlist other = (Playlist) obj;
        return name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ROOT).hashCode();
    }
}

