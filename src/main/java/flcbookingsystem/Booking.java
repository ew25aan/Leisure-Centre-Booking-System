package flcbookingsystem;

/**
 * Represents a booking made by a member for a specific lesson.
 */
public class Booking {

    private static int nextId = 1;

    private int bookingId;
    private Member member;
    private Lesson lesson;

    public Booking(Member member, Lesson lesson) {
        this.bookingId = nextId++;
        this.member = member;
        this.lesson = lesson;
    }

    public int getBookingId() { return bookingId; }
    public Member getMember() { return member; }
    public Lesson getLesson() { return lesson; }

    @Override
    public String toString() {
        return String.format("Booking #%d: %s booked %s",
                bookingId, member.getName(), lesson.toString());
    }
}
