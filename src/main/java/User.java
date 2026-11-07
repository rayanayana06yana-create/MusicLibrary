import java.util.Locale;

public class User {

    private final String username;   // business key
    private final Role role;

    public User(String username, Role role) {
        this.username = Validation.requireText(username, "Username");
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return username.equalsIgnoreCase(other.username);
    }

    @Override
    public int hashCode() {
        return username.toLowerCase(Locale.ROOT).hashCode();
    }
}
