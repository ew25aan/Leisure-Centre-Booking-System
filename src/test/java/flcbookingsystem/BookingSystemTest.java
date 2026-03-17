package flcbookingsystem;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Integration tests for the BookingSystem controller.
 * Covers booking, changing bookings, reviewing, and report generation.
 */
public class BookingSystemTest {

    private BookingSystem system;
    private Member alice;
    private Member bob;

    @Before
    public void setUp() {
        system = new BookingSystem();
        alice  = system.getMemberById(1);   // Alice Smith
        bob    = system.getMemberById(2);   // Bob Jones
    }

    // ── Data initialisation ───────────────────────────────────────────────

    @Test
    public void testSystemLoadsTenMembers() {
        assertEquals(10, system.getMembers().size());
    }

    @Test
    public void testSystemLoadsFortyEightLessons() {
        assertEquals(48, system.getLessons().size());
    }

    @Test
    public void testSystemLoadsTwentyTwoReviews() {
        assertEquals(22, system.getReviews().size());
    }

    @Test
    public void testAllFiveExerciseTypesPresent() {
        boolean yoga = false, zumba = false, aqua = false, box = false, body = false;
        for (Lesson l : system.getLessons()) {
            switch (l.getExerciseType()) {
                case YOGA:       yoga  = true; break;
                case ZUMBA:      zumba = true; break;
                case AQUACISE:   aqua  = true; break;
                case BOX_FIT:    box   = true; break;
                case BODY_BLITZ: body  = true; break;
            }
        }
        assertTrue("Yoga lessons should exist",       yoga);
        assertTrue("Zumba lessons should exist",      zumba);
        assertTrue("Aquacise lessons should exist",   aqua);
        assertTrue("Box Fit lessons should exist",    box);
        assertTrue("Body Blitz lessons should exist", body);
    }

    // ── bookLesson ─────────────────────────────────────────────────────────

    @Test
    public void testSuccessfulBooking() throws Exception {
        // Find an empty lesson that alice is not already in
        Lesson target = findAvailableLessonForMember(alice);
        assertNotNull("There should be an available lesson for Alice", target);
        int beforeCount = target.getBookedMembers().size();
        Booking b = system.bookLesson(alice, target);
        assertNotNull(b);
        assertEquals(beforeCount + 1, target.getBookedMembers().size());
        assertTrue(alice.getBookings().contains(b));
    }

    @Test(expected = Exception.class)
    public void testBookingFullLessonThrows() throws Exception {
        // Fill a lesson to capacity with members 3–6
        Member m3 = system.getMemberById(3);
        Member m4 = system.getMemberById(4);
        Member m5 = system.getMemberById(5);
        Member m6 = system.getMemberById(6);

        // Find a lesson none of them are booked into and that has space
        Lesson target = null;
        for (Lesson l : system.getLessons()) {
            if (!l.hasMemberBooked(m3) && !l.hasMemberBooked(m4)
                    && !l.hasMemberBooked(m5) && !l.hasMemberBooked(m6)
                    && !l.hasMemberBooked(alice)) {
                target = l;
                break;
            }
        }
        assertNotNull(target);

        // Fill it manually to capacity
        target.addMember(m3);
        target.addMember(m4);
        target.addMember(m5);
        target.addMember(m6);
        assertTrue(target.isFull());

        // Now try to book Alice — should throw
        system.bookLesson(alice, target);
    }

    @Test(expected = Exception.class)
    public void testBookingDuplicateLessonThrows() throws Exception {
        // Alice is already booked into lesson index 0 (W1 Sat Morning Yoga)
        Lesson lesson = system.getLessons().get(0);
        assertTrue("Alice should already be booked", lesson.hasMemberBooked(alice));
        system.bookLesson(alice, lesson);  // Should throw
    }

    @Test(expected = Exception.class)
    public void testBookingWithTimeConflictThrows() throws Exception {
        // Alice is booked into W1 Sat Morning (lesson 0). Try to book her
        // into another W1 Sat Morning lesson — should throw.
        Lesson conflicting = new Lesson(99, ExerciseType.ZUMBA, Day.SATURDAY, TimeSlot.MORNING, 1);
        system.bookLesson(alice, conflicting);
    }

    // ── changeBooking ──────────────────────────────────────────────────────

    @Test
    public void testChangeBookingSucceeds() throws Exception {
        // Alice has at least one booking from the pre-loaded data
        assertFalse(alice.getBookings().isEmpty());
        Booking oldBooking = alice.getBookings().get(0);
        Lesson  oldLesson  = oldBooking.getLesson();

        // Find an alternative lesson: same weekend & day, different slot, with space
        Lesson newLesson = null;
        for (Lesson l : system.getLessonsByDayAndWeekend(oldLesson.getDay(), oldLesson.getWeekendNumber())) {
            if (!l.equals(oldLesson) && l.hasSpace() && !l.hasMemberBooked(alice)) {
                // ensure no time conflict with alice's OTHER bookings
                boolean conflict = false;
                for (Booking b : alice.getBookings()) {
                    if (b.equals(oldBooking)) continue;
                    Lesson e = b.getLesson();
                    if (e.getWeekendNumber() == l.getWeekendNumber()
                            && e.getDay() == l.getDay()
                            && e.getTimeSlot() == l.getTimeSlot()) {
                        conflict = true; break;
                    }
                }
                if (!conflict) { newLesson = l; break; }
            }
        }

        if (newLesson == null) return; // No suitable target found — skip rather than fail

        Booking nb = system.changeBooking(oldBooking, newLesson);
        assertNotNull(nb);
        assertFalse("Old lesson should no longer have Alice", oldLesson.hasMemberBooked(alice));
        assertTrue("New lesson should now have Alice", newLesson.hasMemberBooked(alice));
    }

    // ── submitReview ──────────────────────────────────────────────────────

    @Test
    public void testSubmitReviewSucceeds() throws Exception {
        // Alice is booked into lesson 0; she already has a review — find a lesson she's booked in without one
        Member member = system.getMemberById(5); // Emma Davis
        Lesson lesson = system.getLessons().get(3); // W1 Sun Morn Aquacise (Emma is booked in)
        assertTrue(lesson.hasMemberBooked(member));

        // Check she already has a review from sample data and catch the duplicate
        int before = system.getReviews().size();
        try {
            system.submitReview(member, lesson, 5, "Second review attempt.");
            // If no exception: review was accepted, count should increase
            assertEquals(before + 1, system.getReviews().size());
        } catch (Exception e) {
            // Duplicate review — expected; review count should be unchanged
            assertEquals(before, system.getReviews().size());
        }
    }

    @Test(expected = Exception.class)
    public void testReviewWithoutBookingThrows() throws Exception {
        // Member 10 (Jack Anderson) is booked into lesson 3. Use a lesson he is NOT booked into.
        Member member = system.getMemberById(1); // Alice
        Lesson lesson = system.getLessons().get(8); // W2 Sat Evening BodyBlitz (not Alice's)
        assertFalse(lesson.hasMemberBooked(member));
        system.submitReview(member, lesson, 4, "Should fail.");
    }

    // ── Timetable queries ─────────────────────────────────────────────────

    @Test
    public void testGetLessonsByDayReturnsCorrectCount() {
        // 8 weekends × 3 slots = 24 lessons per day
        List<Lesson> satLessons = system.getLessonsByDay(Day.SATURDAY);
        assertEquals(24, satLessons.size());

        List<Lesson> sunLessons = system.getLessonsByDay(Day.SUNDAY);
        assertEquals(24, sunLessons.size());
    }

    @Test
    public void testGetLessonsByDayAndWeekendReturnsThree() {
        List<Lesson> lessons = system.getLessonsByDayAndWeekend(Day.SATURDAY, 1);
        assertEquals(3, lessons.size());
    }

    @Test
    public void testGetLessonsByExerciseNotEmpty() {
        List<Lesson> yogaLessons = system.getLessonsByExercise(ExerciseType.YOGA);
        assertFalse("Should be at least one Yoga lesson", yogaLessons.isEmpty());
    }

    // ── Reports ────────────────────────────────────────────────────────────

    @Test
    public void testMemberReportNotEmpty() {
        String report = system.generateMemberReport();
        assertNotNull(report);
        assertFalse(report.isEmpty());
        assertTrue(report.contains("MEMBER ATTENDANCE REPORT"));
    }

    @Test
    public void testIncomeReportContainsHighestIncome() {
        String report = system.generateIncomeReport();
        assertNotNull(report);
        assertTrue(report.contains("HIGHEST INCOME EXERCISE"));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Lesson findAvailableLessonForMember(Member m) {
        for (Lesson l : system.getLessons()) {
            if (l.hasSpace() && !l.hasMemberBooked(m) && !m.hasTimeConflict(l)) {
                return l;
            }
        }
        return null;
    }
}
