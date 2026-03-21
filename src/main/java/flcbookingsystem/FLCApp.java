package flcbookingsystem;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Furzefield Leisure Centre Group Exercise Booking System.
 *
 * <p>Initialises the booking system with pre-loaded data and launches the
 * Swing GUI on the Event Dispatch Thread.
 */
public class FLCApp {

    public static void main(String[] args) {
        // Use the cross-platform (Metal) L&F so that custom colours on
        // JButton and JTableHeader are honoured on all platforms (the
        // Windows native L&F silently ignores setBackground / setForeground).
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            BookingSystem system = new BookingSystem();
            MainWindow window = new MainWindow(system);
            window.setVisible(true);
        });
    }
}
