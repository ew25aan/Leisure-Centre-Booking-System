package flcbookingsystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main controller class for the Furzefield Leisure Centre booking system.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Maintains the list of members, lessons (timetable), bookings, and reviews.</li>
 *   <li>Enforces business rules: capacity limits, time conflict detection, duplicate reviews.</li>
 *   <li>Provides timetable query methods (by day or by exercise type).</li>
 *   <li>Generates the two required reports (member attendance; highest income exercise).</li>
 * </ul>
 *
 * <p>Pre-loaded data: 10 members, 8 weekends × 6 lessons = 48 lessons, 22 reviews.
 */
public class BookingSystem {

    private List<Member> members;
    private List<Lesson> lessons;
    private List<Booking> bookings;
    private List<Review> reviews;
    private int nextLessonId = 1;

    public BookingSystem() {
        members  = new ArrayList<>();
        lessons  = new ArrayList<>();
        bookings = new ArrayList<>();
        reviews  = new ArrayList<>();

        initializeMembers();
        initializeTimetable();
        initializeSampleData();
    }

    // -----------------------------------------------------------------------
    // Core booking operations
    // -----------------------------------------------------------------------

    /**
     * Books a lesson for the given member.
     *
     * @throws Exception if the lesson is full, the member is already booked,
     *                   or there is a time conflict with an existing booking.
     */
    public Booking bookLesson(Member member, Lesson lesson) throws Exception {
        if (lesson.isFull()) {
            throw new Exception("This lesson is fully booked (max " + Lesson.MAX_CAPACITY + " members).");
        }
        if (lesson.hasMemberBooked(member)) {
            throw new Exception(member.getName() + " is already booked into this lesson.");
        }
        if (member.hasTimeConflict(lesson)) {
            throw new Exception(member.getName() + " already has a booking at this time slot.");
        }
        lesson.addMember(member);
        Booking booking = new Booking(member, lesson);
        bookings.add(booking);
        member.addBooking(booking);
        return booking;
    }

    /**
     * Changes an existing booking to a different lesson.
     * The new lesson must have available space.
     *
     * @throws Exception if the new lesson is full or identical to the current one.
     */
    public Booking changeBooking(Booking existingBooking, Lesson newLesson) throws Exception {
        if (existingBooking.getLesson().equals(newLesson)) {
            throw new Exception("The selected lesson is the same as the current booking.");
        }
        if (newLesson.isFull()) {
            throw new Exception("The chosen lesson is fully booked.");
        }
        Member member = existingBooking.getMember();
        Lesson oldLesson = existingBooking.getLesson();

        // Check time conflict ignoring the old booking
        for (Booking b : member.getBookings()) {
            if (b.equals(existingBooking)) continue;
            Lesson existing = b.getLesson();
            if (existing.getWeekendNumber() == newLesson.getWeekendNumber()
                    && existing.getDay() == newLesson.getDay()
                    && existing.getTimeSlot() == newLesson.getTimeSlot()) {
                throw new Exception("Time conflict: you already have another booking at that slot.");
            }
        }

        // Remove old booking
        oldLesson.removeMember(member);
        member.removeBooking(existingBooking);
        bookings.remove(existingBooking);

        // Create new booking
        newLesson.addMember(member);
        Booking newBooking = new Booking(member, newLesson);
        bookings.add(newBooking);
        member.addBooking(newBooking);
        return newBooking;
    }

    /**
     * Submits a review for a lesson attended by the member.
     *
     * @throws Exception if the member was not booked into the lesson,
     *                   or has already submitted a review for it.
     */
    public Review submitReview(Member member, Lesson lesson, int rating, String comment) throws Exception {
        if (!lesson.hasMemberBooked(member)) {
            throw new Exception("You must be booked into a lesson to review it.");
        }
        for (Review r : reviews) {
            if (r.getMember().equals(member) && r.getLesson().equals(lesson)) {
                throw new Exception("You have already reviewed this lesson.");
            }
        }
        Review review = new Review(member, lesson, rating, comment);
        reviews.add(review);
        lesson.addReview(review);
        return review;
    }

    // -----------------------------------------------------------------------
    // Timetable queries
    // -----------------------------------------------------------------------

    /** Returns all lessons on a given day across all weekends. */
    public List<Lesson> getLessonsByDay(Day day) {
        List<Lesson> result = new ArrayList<>();
        for (Lesson l : lessons) {
            if (l.getDay() == day) result.add(l);
        }
        return result;
    }

    /** Returns the 3 lessons for a specific day on a specific weekend. */
    public List<Lesson> getLessonsByDayAndWeekend(Day day, int weekend) {
        List<Lesson> result = new ArrayList<>();
        for (Lesson l : lessons) {
            if (l.getDay() == day && l.getWeekendNumber() == weekend) result.add(l);
        }
        return result;
    }

    /** Returns all lessons of a given exercise type across all weekends. */
    public List<Lesson> getLessonsByExercise(ExerciseType exerciseType) {
        List<Lesson> result = new ArrayList<>();
        for (Lesson l : lessons) {
            if (l.getExerciseType() == exerciseType) result.add(l);
        }
        return result;
    }

    /** Returns all lessons for a specific weekend. */
    public List<Lesson> getLessonsByWeekend(int weekend) {
        List<Lesson> result = new ArrayList<>();
        for (Lesson l : lessons) {
            if (l.getWeekendNumber() == weekend) result.add(l);
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Reports
    // -----------------------------------------------------------------------

    /**
     * Generates a report showing the number of members and average rating
     * for every lesson across all 8 weekends.
     */
    public String generateMemberReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║          MEMBER ATTENDANCE REPORT — ALL WEEKENDS                           ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════════════════╝\n\n");
        sb.append(String.format("%-4s  %-10s  %-11s  %-12s  %7s  %9s  %-10s%n",
                "W/E", "Day", "Time", "Exercise", "Members", "Capacity", "Avg Rating"));
        sb.append("─".repeat(72)).append("\n");

        for (int w = 1; w <= 8; w++) {
            for (Day d : Day.values()) {
                for (TimeSlot t : TimeSlot.values()) {
                    for (Lesson l : lessons) {
                        if (l.getWeekendNumber() == w
                                && l.getDay() == d
                                && l.getTimeSlot() == t) {
                            String avg = l.getReviews().isEmpty()
                                    ? "  N/A"
                                    : String.format("%.1f/5", l.getAverageRating());
                            sb.append(String.format("%-4d  %-10s  %-11s  %-12s  %7d  %9d  %-10s%n",
                                    w, d.getDisplayName(), t.getDisplayName(),
                                    l.getExerciseType().getDisplayName(),
                                    l.getBookedMembers().size(), Lesson.MAX_CAPACITY, avg));
                        }
                    }
                }
            }
            sb.append("─".repeat(72)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Generates an income report grouped by exercise type, sorted by total income.
     * Highlights the exercise with the highest total income.
     */
    public String generateIncomeReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║           INCOME REPORT BY EXERCISE TYPE                    ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");

        Map<ExerciseType, Double> totalIncome = new LinkedHashMap<>();
        Map<ExerciseType, Integer> totalBookings = new LinkedHashMap<>();
        for (ExerciseType et : ExerciseType.values()) {
            totalIncome.put(et, 0.0);
            totalBookings.put(et, 0);
        }
        for (Lesson l : lessons) {
            ExerciseType et = l.getExerciseType();
            totalIncome.put(et, totalIncome.get(et) + l.getTotalIncome());
            totalBookings.put(et, totalBookings.get(et) + l.getBookedMembers().size());
        }

        // Sort by income descending
        List<Map.Entry<ExerciseType, Double>> sorted = new ArrayList<>(totalIncome.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        sb.append(String.format("%-14s  %-8s  %-15s  %-14s%n",
                "Exercise", "Price", "Total Bookings", "Total Income"));
        sb.append("─".repeat(58)).append("\n");

        ExerciseType highest = null;
        double highestIncome = -1;

        for (Map.Entry<ExerciseType, Double> entry : sorted) {
            ExerciseType et = entry.getKey();
            double income = entry.getValue();
            int count = totalBookings.get(et);
            sb.append(String.format("%-14s  %-8s  %-15d  %-14s%n",
                    et.getDisplayName(),
                    String.format("£%.2f", et.getPrice()),
                    count,
                    String.format("£%.2f", income)));
            if (income > highestIncome) {
                highestIncome = income;
                highest = et;
            }
        }
        sb.append("─".repeat(58)).append("\n\n");
        sb.append(String.format(">>> HIGHEST INCOME EXERCISE: %s (£%.2f total)%n",
                highest != null ? highest.getDisplayName() : "N/A", highestIncome));
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public List<Member> getMembers() { return members; }
    public List<Lesson> getLessons() { return lessons; }
    public List<Booking> getBookings() { return bookings; }
    public List<Review> getReviews() { return reviews; }

    public Member getMemberById(int id) {
        for (Member m : members) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    public Lesson getLessonById(int id) {
        for (Lesson l : lessons) {
            if (l.getId() == id) return l;
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Data initialisation — private helpers
    // -----------------------------------------------------------------------

    private Lesson createLesson(ExerciseType type, Day day, TimeSlot slot, int weekend) {
        return new Lesson(nextLessonId++, type, day, slot, weekend);
    }

    private void initializeMembers() {
        members.add(new Member(1,  "Alice Smith"));
        members.add(new Member(2,  "Bob Jones"));
        members.add(new Member(3,  "Carol White"));
        members.add(new Member(4,  "David Brown"));
        members.add(new Member(5,  "Emma Davis"));
        members.add(new Member(6,  "Frank Miller"));
        members.add(new Member(7,  "Grace Wilson"));
        members.add(new Member(8,  "Henry Moore"));
        members.add(new Member(9,  "Ivy Taylor"));
        members.add(new Member(10, "Jack Anderson"));
    }

    /**
     * Builds the 8-weekend timetable (48 lessons total).
     * Layout per weekend: Sat{Morning, Afternoon, Evening}, Sun{Morning, Afternoon, Evening}.
     * Each weekend uses a different exercise rotation to provide variety.
     */
    private void initializeTimetable() {
        // Index order: SatMorn, SatAftern, SatEven, SunMorn, SunAftern, SunEven
        ExerciseType[][] schedule = {
            // W1
            {ExerciseType.YOGA,       ExerciseType.ZUMBA,      ExerciseType.BOX_FIT,
             ExerciseType.AQUACISE,   ExerciseType.BODY_BLITZ,  ExerciseType.YOGA},
            // W2
            {ExerciseType.ZUMBA,      ExerciseType.AQUACISE,   ExerciseType.BODY_BLITZ,
             ExerciseType.BOX_FIT,    ExerciseType.YOGA,        ExerciseType.ZUMBA},
            // W3
            {ExerciseType.BOX_FIT,    ExerciseType.BODY_BLITZ, ExerciseType.AQUACISE,
             ExerciseType.YOGA,       ExerciseType.ZUMBA,       ExerciseType.BOX_FIT},
            // W4
            {ExerciseType.BODY_BLITZ, ExerciseType.YOGA,       ExerciseType.ZUMBA,
             ExerciseType.ZUMBA,      ExerciseType.AQUACISE,    ExerciseType.BODY_BLITZ},
            // W5
            {ExerciseType.AQUACISE,   ExerciseType.BOX_FIT,    ExerciseType.YOGA,
             ExerciseType.BODY_BLITZ, ExerciseType.BOX_FIT,     ExerciseType.AQUACISE},
            // W6
            {ExerciseType.YOGA,       ExerciseType.AQUACISE,   ExerciseType.BODY_BLITZ,
             ExerciseType.ZUMBA,      ExerciseType.YOGA,        ExerciseType.BOX_FIT},
            // W7
            {ExerciseType.ZUMBA,      ExerciseType.BODY_BLITZ, ExerciseType.YOGA,
             ExerciseType.AQUACISE,   ExerciseType.BOX_FIT,     ExerciseType.BODY_BLITZ},
            // W8
            {ExerciseType.BOX_FIT,    ExerciseType.YOGA,       ExerciseType.AQUACISE,
             ExerciseType.BODY_BLITZ, ExerciseType.ZUMBA,       ExerciseType.YOGA}
        };

        Day[]      days  = Day.values();
        TimeSlot[] slots = TimeSlot.values();

        for (int w = 0; w < 8; w++) {
            int idx = 0;
            for (int d = 0; d < 2; d++) {
                for (int s = 0; s < 3; s++) {
                    lessons.add(createLesson(schedule[w][idx], days[d], slots[s], w + 1));
                    idx++;
                }
            }
        }
    }

    /**
     * Pre-loads sample bookings and reviews so the system demonstrates data
     * across multiple weekends.
     *
     * Members (0-indexed):  0=Alice, 1=Bob, 2=Carol, 3=David, 4=Emma,
     *                       5=Frank, 6=Grace, 7=Henry, 8=Ivy, 9=Jack
     *
     * Lesson indices (0-indexed):
     *   W1: 0=SatMorn(Yoga), 1=SatAftern(Zumba),   2=SatEven(BoxFit),
     *       3=SunMorn(Aqua), 4=SunAftern(BodyBlitz), 5=SunEven(Yoga)
     *   W2: 6=SatMorn(Zumba), 7=SatAftern(Aqua),    8=SatEven(BodyBlitz),
     *       9=SunMorn(BoxFit),10=SunAftern(Yoga),   11=SunEven(Zumba)
     *   W3:12=SatMorn(BoxFit),13=SatAftern(BodyBlitz),14=SatEven(Aqua),
     *      15=SunMorn(Yoga), 16=SunAftern(Zumba),   17=SunEven(BoxFit)
     */
    private void initializeSampleData() {
        // --- Weekend 1 bookings ---
        addBookingSilently(members.get(0), lessons.get(0));   // Alice  → W1 Sat Morn Yoga
        addBookingSilently(members.get(1), lessons.get(0));   // Bob    → W1 Sat Morn Yoga
        addBookingSilently(members.get(8), lessons.get(0));   // Ivy    → W1 Sat Morn Yoga
        addBookingSilently(members.get(2), lessons.get(1));   // Carol  → W1 Sat Aftn Zumba
        addBookingSilently(members.get(3), lessons.get(1));   // David  → W1 Sat Aftn Zumba
        addBookingSilently(members.get(6), lessons.get(2));   // Grace  → W1 Sat Even BoxFit
        addBookingSilently(members.get(4), lessons.get(3));   // Emma   → W1 Sun Morn Aquacise
        addBookingSilently(members.get(9), lessons.get(3));   // Jack   → W1 Sun Morn Aquacise
        addBookingSilently(members.get(5), lessons.get(4));   // Frank  → W1 Sun Aftn BodyBlitz
        addBookingSilently(members.get(7), lessons.get(5));   // Henry  → W1 Sun Even Yoga

        // --- Weekend 2 bookings ---
        addBookingSilently(members.get(0), lessons.get(6));   // Alice  → W2 Sat Morn Zumba
        addBookingSilently(members.get(6), lessons.get(6));   // Grace  → W2 Sat Morn Zumba
        addBookingSilently(members.get(1), lessons.get(7));   // Bob    → W2 Sat Aftn Aquacise
        addBookingSilently(members.get(4), lessons.get(8));   // Emma   → W2 Sat Even BodyBlitz
        addBookingSilently(members.get(2), lessons.get(9));   // Carol  → W2 Sun Morn BoxFit
        addBookingSilently(members.get(7), lessons.get(9));   // Henry  → W2 Sun Morn BoxFit
        addBookingSilently(members.get(3), lessons.get(10));  // David  → W2 Sun Aftn Yoga
        addBookingSilently(members.get(5), lessons.get(11));  // Frank  → W2 Sun Even Zumba

        // --- Weekend 3 bookings ---
        addBookingSilently(members.get(8), lessons.get(12));  // Ivy    → W3 Sat Morn BoxFit
        addBookingSilently(members.get(9), lessons.get(13));  // Jack   → W3 Sat Aftn BodyBlitz
        addBookingSilently(members.get(0), lessons.get(15));  // Alice  → W3 Sun Morn Yoga
        addBookingSilently(members.get(1), lessons.get(16));  // Bob    → W3 Sun Aftn Zumba

        // --- Weekend 1 reviews (10) ---
        addReviewSilently(members.get(0), lessons.get(0), 5, "Fantastic yoga session! Highly recommend.");
        addReviewSilently(members.get(1), lessons.get(0), 4, "Really good class, instructor was brilliant.");
        addReviewSilently(members.get(8), lessons.get(0), 5, "Loved it! Will definitely book again.");
        addReviewSilently(members.get(2), lessons.get(1), 3, "Zumba was okay, a bit crowded at times.");
        addReviewSilently(members.get(3), lessons.get(1), 4, "Fun Zumba class with great moves.");
        addReviewSilently(members.get(4), lessons.get(3), 5, "Aquacise was brilliant — perfect for my joints.");
        addReviewSilently(members.get(9), lessons.get(3), 4, "Really enjoyable session, will come back.");
        addReviewSilently(members.get(5), lessons.get(4), 2, "Body Blitz was not what I expected.");
        addReviewSilently(members.get(6), lessons.get(2), 5, "Intense Box Fit session — loved every minute!");
        addReviewSilently(members.get(7), lessons.get(5), 4, "Relaxing evening yoga, great instructor.");

        // --- Weekend 2 reviews (8) ---
        addReviewSilently(members.get(0), lessons.get(6),  4, "Fun Zumba class, good energy throughout.");
        addReviewSilently(members.get(6), lessons.get(6),  5, "Best Zumba session I've ever attended!");
        addReviewSilently(members.get(1), lessons.get(7),  3, "Aquacise was alright, nothing too special.");
        addReviewSilently(members.get(4), lessons.get(8),  4, "Body Blitz was tough but very rewarding.");
        addReviewSilently(members.get(2), lessons.get(9),  5, "Box Fit was amazing — great full-body workout.");
        addReviewSilently(members.get(7), lessons.get(9),  4, "Really enjoyed Box Fit, much better this time.");
        addReviewSilently(members.get(3), lessons.get(10), 5, "Afternoon yoga is so peaceful and refreshing.");
        addReviewSilently(members.get(5), lessons.get(11), 3, "Zumba was fine, nothing too special.");

        // --- Weekend 3 reviews (4) ---
        addReviewSilently(members.get(8), lessons.get(12), 4, "Box Fit was intense but great exercise!");
        addReviewSilently(members.get(9), lessons.get(13), 5, "Body Blitz exceeded my expectations!");
        addReviewSilently(members.get(0), lessons.get(15), 5, "Morning Yoga is always a joy. Pure bliss.");
        addReviewSilently(members.get(1), lessons.get(16), 4, "Enjoyed the Zumba variety — very lively.");
        // Total: 22 reviews
    }

    /** Books a lesson without throwing — used only for pre-loading sample data. */
    private void addBookingSilently(Member member, Lesson lesson) {
        try {
            bookLesson(member, lesson);
        } catch (Exception ignored) { }
    }

    /** Adds a review directly (bypasses booking check) — for pre-loading sample data. */
    private void addReviewSilently(Member member, Lesson lesson, int rating, String comment) {
        try {
            Review review = new Review(member, lesson, rating, comment);
            reviews.add(review);
            lesson.addReview(review);
        } catch (Exception ignored) { }
    }
}
