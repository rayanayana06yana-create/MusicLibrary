import java.util.Comparator;

public class MediaItemComparators {

    private MediaItemComparators() {
    }

    public static final Comparator<MediaItem> BY_TITLE =
            Comparator.comparing(MediaItem::getTitle, String.CASE_INSENSITIVE_ORDER);

    public static final Comparator<MediaItem> BY_CREATOR =
            Comparator.comparing(MediaItem::getCreator, String.CASE_INSENSITIVE_ORDER);

    public static final Comparator<MediaItem> BY_DURATION =
            Comparator.comparingInt(MediaItem::getDurationMinutes);

    // Multi-criteria: longest first, equal durations sorted by title
    public static final Comparator<MediaItem> BY_DURATION_DESC_THEN_TITLE =
            Comparator.comparingInt(MediaItem::getDurationMinutes).reversed()
                    .thenComparing(BY_TITLE);
}
