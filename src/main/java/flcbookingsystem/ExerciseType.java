package flcbookingsystem;

/**
 * Represents a type of group exercise lesson offered by the Furzefield Leisure Centre.
 * Each exercise has a fixed price regardless of the time it runs.
 */
public enum ExerciseType {
    YOGA("Yoga", 12.00),
    ZUMBA("Zumba", 10.00),
    AQUACISE("Aquacise", 8.00),
    BOX_FIT("Box Fit", 15.00),
    BODY_BLITZ("Body Blitz", 14.00);

    private final String displayName;
    private final double price;

    ExerciseType(String displayName, double price) {
        this.displayName = displayName;
        this.price = price;
    }

    public String getDisplayName() { return displayName; }
    public double getPrice() { return price; }

    @Override
    public String toString() { return displayName + " (£" + String.format("%.2f", price) + ")"; }
}
