public class PodcastPlaylist extends Playlist {

    private static final int RECOMMENDED_LIMIT = 90;

    private final String category;
    private final String language;

    public PodcastPlaylist(String name, User creator, String genre,
                           boolean isPublic, String category, String language) {
        super(name, creator, genre, isPublic);
        this.category = Validation.requireText(category, "Category");
        this.language = Validation.requireText(language, "Language");
    }

    public String getCategory() {
        return category;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String getPlaylistType() {
        return "Podcast";
    }

    @Override
    public int calculateRecommendedListeningTime() {
        return Math.min(getDurationMinutes(), RECOMMENDED_LIMIT);
    }

    @Override
    public void play() {
        System.out.println("Playing podcast playlist: " + getName()
                + " [" + language + "]");
    }
}
