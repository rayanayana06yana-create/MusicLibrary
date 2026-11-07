import java.util.Locale;

public class Validation {

    private Validation() {
    }

    // Returns the trimmed text or throws if it is null/blank.
    public static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        String cleaned = value.trim();
        if (cleaned.indexOf('|') >= 0 || cleaned.indexOf('\n') >= 0 || cleaned.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(fieldName + " cannot contain '|', line breaks.");
        }
        return cleaned;
    }

    // Normalizes a text key for use in Map (case-insensitive lookup).
    public static String key(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
