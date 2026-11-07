public class PersonalPlaylist extends Playlist {

    private static final int RECOMMENDED_LIMIT = 45;

    private boolean shuffleEnabled;

    public PersonalPlaylist(String name, User creator, String genre,
                            boolean isPublic, boolean shuffleEnabled) {
        super(name, creator, genre, isPublic);
        this.shuffleEnabled = shuffleEnabled;
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    public void setShuffleEnabled(boolean shuffleEnabled) {
        this.shuffleEnabled = shuffleEnabled;
    }

    @Override
    public String getPlaylistType() {
        return "Personal";
    }

    @Override
    public int calculateRecommendedListeningTime() {
        return Math.min(getDurationMinutes(), RECOMMENDED_LIMIT);
    }

    @Override
    public void play() {
        System.out.println("Playing personal playlist: " + getName()
                + (shuffleEnabled ? " (shuffle on)" : ""));
    }
}