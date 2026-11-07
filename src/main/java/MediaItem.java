public abstract class MediaItem implements Comparable<MediaItem> {

    private final int id;   // business key
    private final String title;
    private final String creator;
    private final int durationMinutes;

    public MediaItem(int id, String title, String creator, int durationMinutes) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.id = id;
        this.title = Validation.requireText(title, "Title");
        this.creator = Validation.requireText(creator, "Creator");
        this.durationMinutes = durationMinutes;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public abstract String getItemType();

    @Override
    public String toString() {
        return id + ". " + title + " - " + creator
                + " (" + durationMinutes + " min)";
    }

    // equals/hashCode use only the business key (id)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MediaItem)) return false;
        MediaItem other = (MediaItem) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Natural order by the business key (id), consistent with equals().
    @Override
    public int compareTo(MediaItem other) {
        if (other == null) {
            throw new NullPointerException("Cannot compare MediaItem with null.");
        }
        return Integer.compare(this.id, other.id);
    }
}