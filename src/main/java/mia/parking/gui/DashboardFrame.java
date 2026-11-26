/* Team member 1 (Core): [NAME]
 * Team member 2 (Devices): [NAME]
 * Team member 3 (GUI): [NAME]
 */
package mia.parking.gui;

import mia.parking.domain.*;
import mia.parking.dto.*;
import mia.parking.service.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Swing monitoring dashboard. Commands and data are delegated to ParkingService. */
public final class DashboardFrame extends JFrame implements ParkingViewListener {
    private final ParkingService service;
    private final ParkingGridPanel grid = new ParkingGridPanel();
    private final DefaultListModel<SessionView> vehicleModel = new DefaultListModel<>();
    private final DefaultListModel<String> alertModel = new DefaultListModel<>();
    private final JList<SessionView> vehicleList = new JList<>(vehicleModel);
    private final JLabel summary = new JLabel("Loading dashboard…");
    private final JTextField plateField = new JTextField(14);
    private final JLabel searchResult = new JLabel("Search by license plate.");
    private Subscription subscription;

    public DashboardFrame(ParkingService service) {
        super("MIA Airport Parking — Operations Dashboard");
        this.service = Objects.requireNonNull(service, "service");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680)); setSize(1200, 780); setLocationRelativeTo(null);
        buildUi();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { closeSubscription(); }
        });
        refresh();
        subscription = service.subscribe(this);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12)); root.setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Airport Parking Operations"); title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        header.add(title, BorderLayout.NORTH); header.add(summary, BorderLayout.SOUTH); root.add(header, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane(); tabs.addTab("Parking spaces", grid);
        tabs.addTab("Vehicles & alerts", operationsPanel()); tabs.addTab("Devices", devicePanel());
        root.add(tabs, BorderLayout.CENTER); setContentPane(root);
    }

    private JPanel operationsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0)); panel.setBorder(new EmptyBorder(10, 4, 4, 4));
        JPanel left = new JPanel(new BorderLayout(6, 6)); left.setBorder(BorderFactory.createTitledBorder("Current vehicles"));
        vehicleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vehicleList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof SessionView s) setText(s.plateNumber() + "   ·   " + s.spaceId() + "   ·   " + s.status());
                return this;
            }
        });
        left.add(new JScrollPane(vehicleList), BorderLayout.CENTER);
        JPanel controls = new JPanel(new GridLayout(0, 1, 4, 4));
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT)); JButton find = new JButton("Search");
        find.addActionListener(e -> searchPlate()); plateField.addActionListener(e -> searchPlate());
        search.add(plateField); search.add(find); search.add(searchResult); controls.add(search);
        JPanel toggles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton disable = new JButton("Disable selected space"), enable = new JButton("Enable selected space");
        disable.addActionListener(e -> changeOperation(OperationalStatus.DISABLED)); enable.addActionListener(e -> changeOperation(OperationalStatus.ENABLED));
        toggles.add(disable); toggles.add(enable); controls.add(toggles); left.add(controls, BorderLayout.SOUTH);
        JPanel alerts = new JPanel(new BorderLayout()); alerts.setBorder(BorderFactory.createTitledBorder("Alerts"));
        alerts.add(new JScrollPane(new JList<>(alertModel)), BorderLayout.CENTER);
        panel.add(left); panel.add(alerts); return panel;
    }

    private JPanel devicePanel() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.add(new JLabel("Device status will appear here when the device module is connected."), BorderLayout.NORTH);
        return panel;
    }

    private void refresh() {
        try { onDashboardChanged(service.dashboard()); }
        catch (RuntimeException ex) { showError(ErrorCode.INVALID_STATE, readable(ex)); }
    }

    @Override public void onDashboardChanged(DashboardView snapshot) {
        SwingUtilities.invokeLater(() -> {
            grid.setSnapshot(snapshot); vehicleModel.clear();
            snapshot.activeSessions().forEach(vehicleModel::addElement);
            long occupied = snapshot.spaces().stream().filter(s -> s.occupancy() == OccupancyStatus.OCCUPIED).count();
            long reserved = snapshot.spaces().stream().filter(s -> s.occupancy() == OccupancyStatus.RESERVED).count();
            long disabled = snapshot.spaces().stream().filter(s -> s.operation() == OperationalStatus.DISABLED).count();
            int total = snapshot.spaces().size(); double rate = total == 0 ? 0 : 100.0 * (occupied + reserved) / total;
            summary.setText(String.format(Locale.ROOT, "Spaces %d   ·   Occupied %d   ·   Reserved %d   ·   Disabled %d   ·   Occupancy %.1f%%   ·   Updated %s",
                    total, occupied, reserved, disabled, rate, DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault()).format(snapshot.generatedAt())));
        });
    }

    @Override public void onError(ErrorCode code, String message) { SwingUtilities.invokeLater(() -> showError(code, message)); }
    private void showError(ErrorCode code, String message) {
        alertModel.add(0, "[" + code + "] " + message);
        JOptionPane.showMessageDialog(this, message, "Parking operation: " + code, JOptionPane.WARNING_MESSAGE);
    }
    private void searchPlate() {
        String plate = plateField.getText().trim(); if (plate.isEmpty()) { searchResult.setText("Enter a plate."); return; }
        for (int i = 0; i < vehicleModel.size(); i++) {
            SessionView s = vehicleModel.get(i);
            if (s.plateNumber().equalsIgnoreCase(plate)) {
                vehicleList.setSelectedIndex(i); grid.selectSpace(s.spaceId());
                searchResult.setText("Found " + s.spaceId() + " · " + s.status()); return;
            }
        }
        searchResult.setText("No active vehicle found.");
    }
    private void changeOperation(OperationalStatus status) {
        ParkingSpace selected = grid.selectedSpace();
        if (selected == null) { JOptionPane.showMessageDialog(this, "Select a parking space first."); return; }
        try { service.setSpaceOperation(selected.spaceId(), status); refresh(); }
        catch (RuntimeException ex) { showError(ErrorCode.INVALID_STATE, readable(ex)); }
    }
    private static String readable(Throwable ex) { return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(); }
    private void closeSubscription() { if (subscription != null) { subscription.close(); subscription = null; } }
    @Override public void dispose() { closeSubscription(); super.dispose(); }
}
