public class Song extends MediaItem {

    public Song(int id, String title, String artist, int durationMinutes) {
        super(id, title, artist, durationMinutes);
    }

    public String getArtist() {
        return getCreator();
    }

    @Override
    public String getItemType() {
        return "Song";
    }
}
