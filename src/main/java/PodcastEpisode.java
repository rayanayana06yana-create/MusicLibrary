public class PodcastEpisode extends MediaItem {

    private final String category;
    private final String language;

    public PodcastEpisode(int id, String title, String host,
                          int durationMinutes, String category, String language) {
        super(id, title, host, durationMinutes);
        this.category = Validation.requireText(category, "Category");
        this.language = Validation.requireText(language, "Language");
    }

    public String getHost() {
        return getCreator();
    }

    public String getCategory() {
        return category;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String getItemType() {
        return "Podcast Episode";
    }
}
