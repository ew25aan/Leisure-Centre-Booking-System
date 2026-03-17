package flcbookingsystem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main Swing GUI window for the Furzefield Leisure Centre booking system.
 *
 * <p>Contains five tabs:
 * <ol>
 *   <li>Timetable — browse lessons by day+weekend or by exercise name.</li>
 *   <li>Book a Lesson — select a member and book an available lesson.</li>
 *   <li>My Bookings — view a member's bookings and change any booking.</li>
 *   <li>Submit Review — submit a 1–5 star rating and comment after attending.</li>
 *   <li>Reports — view the member attendance report or income report.</li>
 * </ol>
 */
public class MainWindow extends JFrame {

    // Colour palette
    private static final Color BRAND_BLUE   = new Color(0, 102, 153);
    private static final Color BRAND_LIGHT  = new Color(230, 245, 255);
    private static final Color BRAND_ACCENT = new Color(0, 160, 214);
    private static final Color TEXT_WHITE   = Color.WHITE;

    private final BookingSystem system;

    // ── Timetable tab ──────────────────────────────────────────────────────
    private JComboBox<String>      ttSearchType;
    private JComboBox<Integer>     ttWeekendCombo;
    private JComboBox<Day>         ttDayCombo;
    private JComboBox<ExerciseType> ttExerciseCombo;
    private JLabel                 ttWeekendLabel;
    private JLabel                 ttDayLabel;
    private JLabel                 ttExerciseLabel;
    private JTable                 ttTable;
    private DefaultTableModel      ttModel;

    // ── Book a Lesson tab ─────────────────────────────────────────────────
    private JComboBox<Member>      bookMemberCombo;
    private JComboBox<Integer>     bookWeekendCombo;
    private JComboBox<Day>         bookDayCombo;
    private JTable                 bookTable;
    private DefaultTableModel      bookModel;
    private JButton                bookBtn;

    // ── My Bookings tab ───────────────────────────────────────────────────
    private JComboBox<Member>      myMemberCombo;
    private JTable                 myTable;
    private DefaultTableModel      myModel;
    private JButton                changeBtn;

    // ── Submit Review tab ─────────────────────────────────────────────────
    private JComboBox<Member>      rvMemberCombo;
    private JComboBox<Lesson>      rvLessonCombo;
    private JSpinner               rvRatingSpinner;
    private JTextArea              rvCommentArea;
    private JButton                rvSubmitBtn;

    // ── Reports tab ───────────────────────────────────────────────────────
    private JTextArea              reportArea;

    // ══════════════════════════════════════════════════════════════════════
    public MainWindow(BookingSystem system) {
        this.system = system;
        setTitle("Furzefield Leisure Centre — Group Exercise Booking System");
        setSize(960, 680);
        setMinimumSize(new Dimension(800, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("  Timetable  ",   buildTimetableTab());
        tabs.addTab("  Book a Lesson  ", buildBookTab());
        tabs.addTab("  My Bookings  ",  buildMyBookingsTab());
        tabs.addTab("  Submit Review  ", buildReviewTab());
        tabs.addTab("  Reports  ",      buildReportsTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Header
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 14));
        panel.setBackground(BRAND_BLUE);

        JLabel title = new JLabel("Furzefield Leisure Centre");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_WHITE);

        JLabel sub = new JLabel("  |  Group Exercise Booking System");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 15));
        sub.setForeground(new Color(200, 230, 255));

        panel.add(title);
        panel.add(sub);
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tab 1 — Timetable
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildTimetableTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(BRAND_LIGHT);

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controls.setBackground(BRAND_LIGHT);

        ttSearchType = new JComboBox<>(new String[]{"By Day & Weekend", "By Exercise Type"});
        ttSearchType.setFont(new Font("SansSerif", Font.PLAIN, 13));

        ttWeekendLabel = new JLabel("Weekend:");
        ttWeekendCombo = new JComboBox<>();
        for (int i = 1; i <= 8; i++) ttWeekendCombo.addItem(i);
        ttWeekendCombo.setPreferredSize(new Dimension(65, 28));

        ttDayLabel = new JLabel("Day:");
        ttDayCombo = new JComboBox<>(Day.values());

        ttExerciseLabel = new JLabel("Exercise:");
        ttExerciseCombo = new JComboBox<>(ExerciseType.values());
        ttExerciseLabel.setVisible(false);
        ttExerciseCombo.setVisible(false);

        JButton searchBtn = styledButton("Search", BRAND_ACCENT);

        controls.add(new JLabel("Search:"));
        controls.add(ttSearchType);
        controls.add(ttWeekendLabel);
        controls.add(ttWeekendCombo);
        controls.add(ttDayLabel);
        controls.add(ttDayCombo);
        controls.add(ttExerciseLabel);
        controls.add(ttExerciseCombo);
        controls.add(searchBtn);
        panel.add(controls, BorderLayout.NORTH);

        // Table
        String[] cols = {"Weekend", "Day", "Time", "Exercise", "Price", "Booked", "Spaces Left", "Avg Rating"};
        ttModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        ttTable = styledTable(ttModel);
        panel.add(new JScrollPane(ttTable), BorderLayout.CENTER);

        // Events
        ttSearchType.addActionListener(e -> {
            boolean byDay = ttSearchType.getSelectedIndex() == 0;
            ttWeekendLabel.setVisible(byDay);
            ttWeekendCombo.setVisible(byDay);
            ttDayLabel.setVisible(byDay);
            ttDayCombo.setVisible(byDay);
            ttExerciseLabel.setVisible(!byDay);
            ttExerciseCombo.setVisible(!byDay);
        });

        searchBtn.addActionListener(e -> refreshTimetable());

        // Show all lessons on first load
        populateTimetableFull();
        return panel;
    }

    private void refreshTimetable() {
        ttModel.setRowCount(0);
        List<Lesson> results;
        if (ttSearchType.getSelectedIndex() == 0) {
            Day day = (Day) ttDayCombo.getSelectedItem();
            int weekend = (Integer) ttWeekendCombo.getSelectedItem();
            results = system.getLessonsByDayAndWeekend(day, weekend);
        } else {
            ExerciseType et = (ExerciseType) ttExerciseCombo.getSelectedItem();
            results = system.getLessonsByExercise(et);
        }
        for (Lesson l : results) {
            String avg = l.getReviews().isEmpty()
                    ? "N/A" : String.format("%.1f / 5", l.getAverageRating());
            ttModel.addRow(new Object[]{
                    l.getWeekendNumber(),
                    l.getDay().getDisplayName(),
                    l.getTimeSlot().toString(),
                    l.getExerciseType().getDisplayName(),
                    String.format("£%.2f", l.getPrice()),
                    l.getBookedMembers().size(),
                    l.getAvailableSpaces(),
                    avg
            });
        }
    }

    private void populateTimetableFull() {
        ttModel.setRowCount(0);
        for (Lesson l : system.getLessons()) {
            String avg = l.getReviews().isEmpty()
                    ? "N/A" : String.format("%.1f / 5", l.getAverageRating());
            ttModel.addRow(new Object[]{
                    l.getWeekendNumber(),
                    l.getDay().getDisplayName(),
                    l.getTimeSlot().toString(),
                    l.getExerciseType().getDisplayName(),
                    String.format("£%.2f", l.getPrice()),
                    l.getBookedMembers().size(),
                    l.getAvailableSpaces(),
                    avg
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tab 2 — Book a Lesson
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildBookTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(BRAND_LIGHT);

        // Top controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controls.setBackground(BRAND_LIGHT);

        bookMemberCombo = new JComboBox<>();
        for (Member m : system.getMembers()) bookMemberCombo.addItem(m);
        bookMemberCombo.setRenderer(new MemberRenderer());

        bookWeekendCombo = new JComboBox<>();
        for (int i = 1; i <= 8; i++) bookWeekendCombo.addItem(i);
        bookWeekendCombo.setPreferredSize(new Dimension(65, 28));

        bookDayCombo = new JComboBox<>(Day.values());

        JButton loadBtn = styledButton("Load Lessons", BRAND_ACCENT);

        controls.add(new JLabel("Member:"));
        controls.add(bookMemberCombo);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(new JLabel("Weekend:"));
        controls.add(bookWeekendCombo);
        controls.add(new JLabel("Day:"));
        controls.add(bookDayCombo);
        controls.add(loadBtn);
        panel.add(controls, BorderLayout.NORTH);

        // Table of available lessons
        String[] cols = {"ID", "Time", "Exercise", "Price", "Spaces Left", "Already Booked"};
        bookModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookTable = styledTable(bookModel);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // Bottom — book button
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(BRAND_LIGHT);
        bookBtn = styledButton("Book Selected Lesson", new Color(0, 153, 76));
        south.add(bookBtn);
        panel.add(south, BorderLayout.SOUTH);

        // Events
        loadBtn.addActionListener(e -> refreshBookTable());
        bookBtn.addActionListener(e -> doBookLesson());

        return panel;
    }

    private void refreshBookTable() {
        bookModel.setRowCount(0);
        Day day = (Day) bookDayCombo.getSelectedItem();
        int weekend = (Integer) bookWeekendCombo.getSelectedItem();
        Member member = (Member) bookMemberCombo.getSelectedItem();
        List<Lesson> lessons = system.getLessonsByDayAndWeekend(day, weekend);
        for (Lesson l : lessons) {
            boolean alreadyBooked = l.hasMemberBooked(member);
            bookModel.addRow(new Object[]{
                    l.getId(),
                    l.getTimeSlot().toString(),
                    l.getExerciseType().getDisplayName(),
                    String.format("£%.2f", l.getPrice()),
                    l.getAvailableSpaces(),
                    alreadyBooked ? "Yes" : "No"
            });
        }
    }

    private void doBookLesson() {
        int row = bookTable.getSelectedRow();
        if (row < 0) { showInfo("Please select a lesson from the table."); return; }
        int lessonId = (Integer) bookModel.getValueAt(row, 0);
        Lesson lesson = system.getLessonById(lessonId);
        Member member = (Member) bookMemberCombo.getSelectedItem();
        try {
            Booking b = system.bookLesson(member, lesson);
            showSuccess("Booking confirmed!\n" + b.toString());
            refreshBookTable();
            refreshMyBookings();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tab 3 — My Bookings
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildMyBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(BRAND_LIGHT);

        // Top controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controls.setBackground(BRAND_LIGHT);

        myMemberCombo = new JComboBox<>();
        for (Member m : system.getMembers()) myMemberCombo.addItem(m);
        myMemberCombo.setRenderer(new MemberRenderer());
        JButton loadBtn = styledButton("Load My Bookings", BRAND_ACCENT);

        controls.add(new JLabel("Member:"));
        controls.add(myMemberCombo);
        controls.add(loadBtn);
        panel.add(controls, BorderLayout.NORTH);

        // Table
        String[] cols = {"Booking ID", "Weekend", "Day", "Time", "Exercise", "Price"};
        myModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        myTable = styledTable(myModel);
        panel.add(new JScrollPane(myTable), BorderLayout.CENTER);

        // Bottom
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(BRAND_LIGHT);
        changeBtn = styledButton("Change Selected Booking", new Color(204, 102, 0));
        south.add(changeBtn);
        panel.add(south, BorderLayout.SOUTH);

        // Events
        loadBtn.addActionListener(e -> refreshMyBookings());
        changeBtn.addActionListener(e -> doChangeBooking());

        return panel;
    }

    private void refreshMyBookings() {
        myModel.setRowCount(0);
        Member member = (Member) myMemberCombo.getSelectedItem();
        if (member == null) return;
        for (Booking b : member.getBookings()) {
            Lesson l = b.getLesson();
            myModel.addRow(new Object[]{
                    b.getBookingId(),
                    l.getWeekendNumber(),
                    l.getDay().getDisplayName(),
                    l.getTimeSlot().toString(),
                    l.getExerciseType().getDisplayName(),
                    String.format("£%.2f", l.getPrice())
            });
        }
    }

    private void doChangeBooking() {
        int row = myTable.getSelectedRow();
        if (row < 0) { showInfo("Please select a booking to change."); return; }

        Member member = (Member) myMemberCombo.getSelectedItem();
        int bookingId = (Integer) myModel.getValueAt(row, 0);

        // Find the booking
        Booking target = null;
        for (Booking b : member.getBookings()) {
            if (b.getBookingId() == bookingId) { target = b; break; }
        }
        if (target == null) { showError("Booking not found."); return; }

        // Show dialog listing all lessons with space (same weekend, same day)
        Lesson current = target.getLesson();
        List<Lesson> candidates = system.getLessonsByDayAndWeekend(
                current.getDay(), current.getWeekendNumber());

        // Remove the current lesson and full lessons
        candidates.removeIf(l -> l.equals(current) || l.isFull());

        if (candidates.isEmpty()) {
            showInfo("No other available lessons on that day/weekend.");
            return;
        }

        // Build a small dialog
        JDialog dialog = new JDialog(this, "Change Booking", true);
        dialog.setSize(520, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JLabel info = new JLabel("  Select a new lesson for Weekend " + current.getWeekendNumber()
                + ", " + current.getDay().getDisplayName() + ":");
        info.setFont(new Font("SansSerif", Font.BOLD, 13));
        dialog.add(info, BorderLayout.NORTH);

        String[] cols = {"ID", "Time", "Exercise", "Price", "Spaces"};
        DefaultTableModel dlgModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Lesson l : candidates) {
            dlgModel.addRow(new Object[]{
                    l.getId(), l.getTimeSlot().toString(),
                    l.getExerciseType().getDisplayName(),
                    String.format("£%.2f", l.getPrice()), l.getAvailableSpaces()
            });
        }
        JTable dlgTable = styledTable(dlgModel);
        dialog.add(new JScrollPane(dlgTable), BorderLayout.CENTER);

        JButton confirmBtn = styledButton("Confirm Change", new Color(0, 153, 76));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(confirmBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        final Booking[] finalTarget = {target};
        confirmBtn.addActionListener(e -> {
            int selRow = dlgTable.getSelectedRow();
            if (selRow < 0) { showInfo("Please select a lesson."); return; }
            int newId = (Integer) dlgModel.getValueAt(selRow, 0);
            Lesson newLesson = system.getLessonById(newId);
            try {
                Booking nb = system.changeBooking(finalTarget[0], newLesson);
                showSuccess("Booking changed!\n" + nb.toString());
                refreshMyBookings();
                dialog.dispose();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tab 4 — Submit Review
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildReviewTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setBackground(BRAND_LIGHT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BRAND_LIGHT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;

        // Member
        rvMemberCombo = new JComboBox<>();
        for (Member m : system.getMembers()) rvMemberCombo.addItem(m);
        rvMemberCombo.setRenderer(new MemberRenderer());
        rvMemberCombo.setPreferredSize(new Dimension(220, 28));
        addFormRow(form, gc, 0, "Member:", rvMemberCombo);

        // Lesson
        rvLessonCombo = new JComboBox<>();
        rvLessonCombo.setPreferredSize(new Dimension(340, 28));
        rvLessonCombo.setRenderer(new LessonRenderer());
        addFormRow(form, gc, 1, "Lesson:", rvLessonCombo);

        // Rating
        rvRatingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        rvRatingSpinner.setPreferredSize(new Dimension(60, 28));
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ratingPanel.setBackground(BRAND_LIGHT);
        ratingPanel.add(rvRatingSpinner);
        ratingPanel.add(new JLabel("   (1 = Very Dissatisfied, 5 = Very Satisfied)"));
        addFormRow(form, gc, 2, "Rating:", ratingPanel);

        // Comment
        rvCommentArea = new JTextArea(4, 30);
        rvCommentArea.setLineWrap(true);
        rvCommentArea.setWrapStyleWord(true);
        rvCommentArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane commentScroll = new JScrollPane(rvCommentArea);
        commentScroll.setPreferredSize(new Dimension(340, 90));
        addFormRow(form, gc, 3, "Comment:", commentScroll);

        panel.add(form, BorderLayout.CENTER);

        // Submit button
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(BRAND_LIGHT);
        rvSubmitBtn = styledButton("Submit Review", new Color(0, 153, 76));
        south.add(rvSubmitBtn);
        panel.add(south, BorderLayout.SOUTH);

        // Events — update lesson combo when member changes
        rvMemberCombo.addActionListener(e -> refreshReviewLessonCombo());
        rvSubmitBtn.addActionListener(e -> doSubmitReview());

        // Initial load
        refreshReviewLessonCombo();
        return panel;
    }

    private void refreshReviewLessonCombo() {
        rvLessonCombo.removeAllItems();
        Member member = (Member) rvMemberCombo.getSelectedItem();
        if (member == null) return;
        for (Booking b : member.getBookings()) {
            rvLessonCombo.addItem(b.getLesson());
        }
    }

    private void doSubmitReview() {
        Member member = (Member) rvMemberCombo.getSelectedItem();
        Lesson lesson = (Lesson) rvLessonCombo.getSelectedItem();
        if (lesson == null) { showInfo("No lessons available to review for this member."); return; }
        int rating = (Integer) rvRatingSpinner.getValue();
        String comment = rvCommentArea.getText().trim();
        if (comment.isEmpty()) { showInfo("Please enter a comment before submitting."); return; }
        try {
            Review rv = system.submitReview(member, lesson, rating, comment);
            showSuccess("Review submitted!\n" + rv.toString());
            rvCommentArea.setText("");
            rvRatingSpinner.setValue(5);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Tab 5 — Reports
    // ══════════════════════════════════════════════════════════════════════

    private JPanel buildReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(BRAND_LIGHT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        btnPanel.setBackground(BRAND_LIGHT);

        JButton attendanceBtn = styledButton("Member Attendance Report", BRAND_ACCENT);
        JButton incomeBtn     = styledButton("Income Report", new Color(153, 51, 0));

        btnPanel.add(attendanceBtn);
        btnPanel.add(incomeBtn);
        panel.add(btnPanel, BorderLayout.NORTH);

        reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        reportArea.setBackground(Color.WHITE);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        attendanceBtn.addActionListener(e -> reportArea.setText(system.generateMemberReport()));
        incomeBtn.addActionListener(e -> reportArea.setText(system.generateIncomeReport()));

        // Pre-populate income report
        reportArea.setText(system.generateIncomeReport());
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setOpaque(true);
        return btn;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(BRAND_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(BRAND_ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(200, 220, 240));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(lbl, gc);
        gc.gridx = 1;
        panel.add(comp, gc);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Custom renderers
    private static class MemberRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Member) {
                Member m = (Member) value;
                setText(m.getName() + " (#" + m.getId() + ")");
            }
            return this;
        }
    }

    private static class LessonRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Lesson) {
                Lesson l = (Lesson) value;
                setText(String.format("W%d %s %s — %s (£%.2f)",
                        l.getWeekendNumber(), l.getDay().getDisplayName(),
                        l.getTimeSlot().getDisplayName(),
                        l.getExerciseType().getDisplayName(), l.getPrice()));
            }
            return this;
        }
    }
}
