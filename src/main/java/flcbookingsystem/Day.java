package flcbookingsystem;

/**
 * Represents the two weekend days on which lessons are held.
 */
public enum Day {
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private final String displayName;

    Day(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
