package flcbookingsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single group exercise lesson offered by the Furzefield Leisure Centre.
 * Each lesson has a specific exercise type, day, time slot, and weekend number.
 * A lesson can accommodate at most MAX_CAPACITY (4) members.
 */
public class Lesson {

    public static final int MAX_CAPACITY = 4;

    private int id;
    private ExerciseType exerciseType;
    private Day day;
    private TimeSlot timeSlot;
    private int weekendNumber;
    private List<Member> bookedMembers;
    private List<Review> reviews;

    public Lesson(int id, ExerciseType exerciseType, Day day, TimeSlot timeSlot, int weekendNumber) {
        this.id = id;
        this.exerciseType = exerciseType;
        this.day = day;
        this.timeSlot = timeSlot;
        this.weekendNumber = weekendNumber;
        this.bookedMembers = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    // --- Getters ---
    public int getId() { return id; }
    public ExerciseType getExerciseType() { return exerciseType; }
    public Day getDay() { return day; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public int getWeekendNumber() { return weekendNumber; }
    public List<Member> getBookedMembers() { return bookedMembers; }
    public List<Review> getReviews() { return reviews; }

    // --- Capacity ---
    public boolean isFull() { return bookedMembers.size() >= MAX_CAPACITY; }
    public boolean hasSpace() { return bookedMembers.size() < MAX_CAPACITY; }
    public int getAvailableSpaces() { return MAX_CAPACITY - bookedMembers.size(); }
    public double getPrice() { return exerciseType.getPrice(); }

    /**
     * Adds a member to this lesson if there is space and they are not already booked.
     * @return true if successfully added
     */
    public boolean addMember(Member member) {
        if (!isFull() && !bookedMembers.contains(member)) {
            bookedMembers.add(member);
            return true;
        }
        return false;
    }

    public boolean removeMember(Member member) {
        return bookedMembers.remove(member);
    }

    public boolean hasMemberBooked(Member member) {
        return bookedMembers.contains(member);
    }

    public void addReview(Review review) {
        reviews.add(review);
    }

    /**
     * Returns the average rating across all reviews for this lesson.
     * Returns 0.0 if no reviews have been submitted.
     */
    public double getAverageRating() {
        if (reviews.isEmpty()) return 0.0;
        double total = 0;
        for (Review r : reviews) total += r.getRating();
        return total / reviews.size();
    }

    /** Returns total income generated: number of bookings × lesson price. */
    public double getTotalIncome() {
        return bookedMembers.size() * exerciseType.getPrice();
    }

    @Override
    public String toString() {
        return String.format("W%d | %s | %s | %s | £%.2f | %d/%d spaces",
                weekendNumber, day.getDisplayName(), timeSlot.getDisplayName(),
                exerciseType.getDisplayName(), exerciseType.getPrice(),
                bookedMembers.size(), MAX_CAPACITY);
    }
}
