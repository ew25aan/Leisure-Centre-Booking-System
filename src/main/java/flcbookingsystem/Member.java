package flcbookingsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a member of the Furzefield Leisure Centre.
 * A member can hold multiple bookings, subject to no time conflicts.
 */
public class Member {
    private int id;
    private String name;
    private List<Booking> bookings;

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
        this.bookings = new ArrayList<>();
    }

    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Booking> getBookings() { return bookings; }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    public boolean removeBooking(Booking booking) {
        return bookings.remove(booking);
    }

    /**
     * Checks whether this member already has a booking at the same
     * weekend number, day, and time slot as the given lesson.
     *
     * @param lesson the lesson to check for a time conflict
     * @return true if there is a conflict, false otherwise
     */
    public boolean hasTimeConflict(Lesson lesson) {
        for (Booking b : bookings) {
            Lesson existing = b.getLesson();
            if (existing.getWeekendNumber() == lesson.getWeekendNumber()
                    && existing.getDay() == lesson.getDay()
                    && existing.getTimeSlot() == lesson.getTimeSlot()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the Booking object for the specified lesson, or null if not found.
     */
    public Booking getBookingForLesson(Lesson lesson) {
        for (Booking b : bookings) {
            if (b.getLesson().equals(lesson)) return b;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Member #" + id + ": " + name;
    }
}
