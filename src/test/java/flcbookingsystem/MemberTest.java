package flcbookingsystem;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Member class.
 * Covers time-conflict detection and booking management.
 */
public class MemberTest {

    private Member alice;
    private Lesson yogaSatMorn;   // W1 Sat Morning
    private Lesson zumbaSatMorn;  // W1 Sat Morning (conflict with yoga)
    private Lesson yogaSatAftn;   // W1 Sat Afternoon (no conflict)
    private Lesson yogaSatMornW2; // W2 Sat Morning (different weekend, no conflict)

    @Before
    public void setUp() {
        alice = new Member(1, "Alice");

        yogaSatMorn   = new Lesson(1, ExerciseType.YOGA,  Day.SATURDAY, TimeSlot.MORNING,   1);
        zumbaSatMorn  = new Lesson(2, ExerciseType.ZUMBA, Day.SATURDAY, TimeSlot.MORNING,   1);
        yogaSatAftn   = new Lesson(3, ExerciseType.YOGA,  Day.SATURDAY, TimeSlot.AFTERNOON, 1);
        yogaSatMornW2 = new Lesson(4, ExerciseType.YOGA,  Day.SATURDAY, TimeSlot.MORNING,   2);
    }

    @Test
    public void testNewMemberHasNoBookings() {
        assertEquals(0, alice.getBookings().size());
    }

    @Test
    public void testNoConflictWhenNoBookings() {
        assertFalse(alice.hasTimeConflict(yogaSatMorn));
    }

    @Test
    public void testConflictDetectedForSameSlot() {
        // Book Alice into W1 Sat Morning Yoga
        yogaSatMorn.addMember(alice);
        Booking b = new Booking(alice, yogaSatMorn);
        alice.addBooking(b);

        // Trying to book W1 Sat Morning Zumba should conflict
        assertTrue("Same weekend, day, time should conflict",
                alice.hasTimeConflict(zumbaSatMorn));
    }

    @Test
    public void testNoConflictForDifferentTimeSlot() {
        yogaSatMorn.addMember(alice);
        Booking b = new Booking(alice, yogaSatMorn);
        alice.addBooking(b);

        // Saturday Afternoon is a different slot, no conflict
        assertFalse("Different time slot should not conflict",
                alice.hasTimeConflict(yogaSatAftn));
    }

    @Test
    public void testNoConflictForDifferentWeekend() {
        yogaSatMorn.addMember(alice);
        Booking b = new Booking(alice, yogaSatMorn);
        alice.addBooking(b);

        // same day and time but Weekend 2, no conflict
        assertFalse("Different weekend should not conflict",
                alice.hasTimeConflict(yogaSatMornW2));
    }

    @Test
    public void testAddAndRemoveBooking() {
        Booking b = new Booking(alice, yogaSatMorn);
        alice.addBooking(b);
        assertEquals(1, alice.getBookings().size());

        alice.removeBooking(b);
        assertEquals(0, alice.getBookings().size());
    }

    @Test
    public void testGetBookingForLesson() {
        Booking b = new Booking(alice, yogaSatMorn);
        alice.addBooking(b);

        assertNotNull(alice.getBookingForLesson(yogaSatMorn));
        assertNull(alice.getBookingForLesson(yogaSatAftn));
    }

    @Test
    public void testToStringContainsMemberInfo() {
        String s = alice.toString();
        assertTrue(s.contains("Alice"));
        assertTrue(s.contains("1"));
    }
}
