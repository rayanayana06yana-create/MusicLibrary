import java.util.HashSet;
import java.util.Set;

public class CollaborativePlaylist extends Playlist {

    private static final int RECOMMENDED_LIMIT = 60;

    private final Set<User> collaborators = new HashSet<>();   // N:M with User
    private final int maxCollaborators;

    public CollaborativePlaylist(String name, User creator, String genre,
                                 boolean isPublic, int maxCollaborators) {
        super(name, creator, genre, isPublic);
        if (maxCollaborators < 1) {
            throw new IllegalArgumentException("Maximum collaborators must be at least 1.");
        }
        this.maxCollaborators = maxCollaborators;
        collaborators.add(creator);
    }

    // Returns false if the user is already a collaborator.
    // Throws if the limit is reached.
    public boolean addCollaborator(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (collaborators.contains(user)) {
            return false;
        }
        if (collaborators.size() >= maxCollaborators) {
            throw new IllegalStateException("Maximum collaborators reached: " + maxCollaborators);
        }
        return collaborators.add(user);
    }

    public boolean removeCollaborator(User user) {
        return collaborators.remove(user);
    }

    public Set<User> getCollaborators() {
        return new HashSet<>(collaborators);
    }

    public int getMaxCollaborators() {
        return maxCollaborators;
    }

    @Override
    public String getPlaylistType() {
        return "Collaborative";
    }

    @Override
    public int calculateRecommendedListeningTime() {
        return Math.min(getDurationMinutes(), RECOMMENDED_LIMIT);
    }

    @Override
    public void play() {
        System.out.println("Playing collaborative playlist: " + getName()
                + " (" + collaborators.size() + " collaborators)");
    }
}