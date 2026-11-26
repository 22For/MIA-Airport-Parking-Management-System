/* Team member 1 (Core): [NAME]
 * Team member 2 (Devices): [NAME]
 * Team member 3 (GUI): [NAME]
 */
package mia.parking.gui;

import javax.swing.*;

/** Standalone demo launcher for the Swing GUI with preset data. */
public final class ParkingDashboardApp {
    private ParkingDashboardApp() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            DashboardFrame frame = new DashboardFrame(new MockParkingService());
            frame.setVisible(true);
        });
    }
}
