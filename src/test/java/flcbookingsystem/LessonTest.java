package flcbookingsystem;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Lesson class.
 * Covers capacity limits, member management, and average rating calculations.
 */
public class LessonTest {

    private Lesson lesson;
    private Member alice;
    private Member bob;
    private Member carol;
    private Member david;
    private Member extra;

    @Before
    public void setUp() {
        lesson = new Lesson(1, ExerciseType.YOGA, Day.SATURDAY, TimeSlot.MORNING, 1);
        alice  = new Member(1, "Alice");
        bob    = new Member(2, "Bob");
        carol  = new Member(3, "Carol");
        david  = new Member(4, "David");
        extra  = new Member(5, "Extra");
    }

    // ── Capacity tests ────────────────────────────────────────────────────

    @Test
    public void testLessonStartsEmpty() {
        assertEquals(0, lesson.getBookedMembers().size());
        assertTrue(lesson.hasSpace());
        assertFalse(lesson.isFull());
        assertEquals(Lesson.MAX_CAPACITY, lesson.getAvailableSpaces());
    }

    @Test
    public void testAddMemberIncreasesCount() {
        lesson.addMember(alice);
        assertEquals(1, lesson.getBookedMembers().size());
        assertEquals(Lesson.MAX_CAPACITY - 1, lesson.getAvailableSpaces());
    }

    @Test
    public void testLessonFullAfterFourMembers() {
        lesson.addMember(alice);
        lesson.addMember(bob);
        lesson.addMember(carol);
        lesson.addMember(david);
        assertTrue(lesson.isFull());
        assertFalse(lesson.hasSpace());
        assertEquals(0, lesson.getAvailableSpaces());
    }

    @Test
    public void testCannotAddFifthMember() {
        lesson.addMember(alice);
        lesson.addMember(bob);
        lesson.addMember(carol);
        lesson.addMember(david);
        boolean result = lesson.addMember(extra);
        assertFalse("Should not be able to add a 5th member", result);
        assertEquals(4, lesson.getBookedMembers().size());
    }

    @Test
    public void testCannotAddDuplicateMember() {
        lesson.addMember(alice);
        boolean result = lesson.addMember(alice);
        assertFalse("Duplicate member should not be added", result);
        assertEquals(1, lesson.getBookedMembers().size());
    }

    @Test
    public void testRemoveMember() {
        lesson.addMember(alice);
        lesson.addMember(bob);
        lesson.removeMember(alice);
        assertFalse(lesson.hasMemberBooked(alice));
        assertEquals(1, lesson.getBookedMembers().size());
    }

    @Test
    public void testHasMemberBooked() {
        assertFalse(lesson.hasMemberBooked(alice));
        lesson.addMember(alice);
        assertTrue(lesson.hasMemberBooked(alice));
    }

    // ── Pricing tests ─────────────────────────────────────────────────────

    @Test
    public void testLessonPriceMatchesExerciseType() {
        assertEquals(ExerciseType.YOGA.getPrice(), lesson.getPrice(), 0.001);
    }

    @Test
    public void testTotalIncomeZeroWhenEmpty() {
        assertEquals(0.0, lesson.getTotalIncome(), 0.001);
    }

    @Test
    public void testTotalIncomeWithMembers() {
        lesson.addMember(alice);
        lesson.addMember(bob);
        double expected = 2 * ExerciseType.YOGA.getPrice();
        assertEquals(expected, lesson.getTotalIncome(), 0.001);
    }

    // ── Review / rating tests ─────────────────────────────────────────────

    @Test
    public void testAverageRatingZeroWhenNoReviews() {
        assertEquals(0.0, lesson.getAverageRating(), 0.001);
    }

    @Test
    public void testAverageRatingWithOneReview() {
        lesson.addMember(alice);
        lesson.addReview(new Review(alice, lesson, 4, "Good class."));
        assertEquals(4.0, lesson.getAverageRating(), 0.001);
    }

    @Test
    public void testAverageRatingWithMultipleReviews() {
        lesson.addMember(alice);
        lesson.addMember(bob);
        lesson.addReview(new Review(alice, lesson, 5, "Excellent!"));
        lesson.addReview(new Review(bob,   lesson, 3, "Average."));
        assertEquals(4.0, lesson.getAverageRating(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReviewRatingBelowOnethrowsException() {
        new Review(alice, lesson, 0, "Bad");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReviewRatingAboveFiveThrowsException() {
        new Review(alice, lesson, 6, "Too high");
    }
}
