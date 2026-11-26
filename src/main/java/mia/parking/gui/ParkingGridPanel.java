/* Team member 1 (Core): [NAME]
 * Team member 2 (Devices): [NAME]
 * Team member 3 (GUI): [NAME]
 */
package mia.parking.gui;

import mia.parking.domain.*;
import mia.parking.dto.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Zone-tabbed parking visualization with 50 spaces per page. */
public final class ParkingGridPanel extends JPanel {
    private static final int PAGE_SIZE = 50;
    private final JTabbedPane zoneTabs = new JTabbedPane();
    private final Map<String, ParkingSpace> metadata = new HashMap<>();
    private final Map<String, SpaceView> views = new HashMap<>();
    private final Map<String, String> plates = new HashMap<>();
    private final Map<String, JPanel> grids = new LinkedHashMap<>();
    private final Map<String, Integer> pages = new HashMap<>();
    private String selectedId;

    public ParkingGridPanel() {
        super(new BorderLayout(8, 8)); add(zoneTabs, BorderLayout.CENTER); add(legend(), BorderLayout.SOUTH);
    }

    public void setSnapshot(DashboardView snapshot) {
        metadata.clear(); views.clear(); plates.clear(); grids.clear(); pages.clear();
        for (SpaceView s : snapshot.spaces()) {
            views.put(s.spaceId(), s);
            metadata.put(s.spaceId(), new ParkingSpace(s.spaceId(), s.hall(), s.zone(), s.floor(), s.chargingAvailable(), s.accessible()));
        }
        for (SessionView s : snapshot.activeSessions()) plates.put(s.spaceId(), s.plateNumber());
        zoneTabs.removeAll();
        Map<String, List<SpaceView>> byZone = new TreeMap<>();
        snapshot.spaces().forEach(s -> byZone.computeIfAbsent(s.zone(), ignored -> new ArrayList<>()).add(s));
        for (Map.Entry<String, List<SpaceView>> entry : byZone.entrySet()) {
            entry.getValue().sort(Comparator.comparing(SpaceView::spaceId));
            if (entry.getValue().size() <= PAGE_SIZE) addZoneTab(entry.getKey(), entry.getValue(), 0);
            else for (int start = 0, part = 1; start < entry.getValue().size(); start += PAGE_SIZE, part++)
                addZoneTab(entry.getKey() + " — Part " + part, entry.getValue().subList(start, Math.min(start + PAGE_SIZE, entry.getValue().size())), part - 1);
        }
        if (byZone.isEmpty()) zoneTabs.addTab("No spaces", new JLabel("No parking spaces are available."));
    }

    private void addZoneTab(String title, List<SpaceView> spaces, int page) {
        JPanel grid = new JPanel(new GridLayout(0, 5, 8, 8)); grid.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        for (SpaceView s : spaces) grid.add(spaceTile(s));
        JScrollPane scroll = new JScrollPane(grid); scroll.getVerticalScrollBar().setUnitIncrement(16);
        zoneTabs.addTab(title, scroll); grids.put(title, grid); pages.put(title, page);
    }

    private JButton spaceTile(SpaceView s) {
        JButton b = new JButton("▣\n" + s.spaceId()); b.setVerticalTextPosition(SwingConstants.BOTTOM); b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setOpaque(true); b.setFocusPainted(false); b.setPreferredSize(new Dimension(105, 62));
        boolean disabled = s.operation() == OperationalStatus.DISABLED;
        Color color = disabled ? new Color(83, 92, 104) : switch (s.occupancy()) {
            case FREE -> new Color(229, 235, 242); case RESERVED -> new Color(255, 230, 153); case OCCUPIED -> new Color(150, 225, 184);
        };
        b.setBackground(color); b.setForeground(disabled || s.occupancy() == OccupancyStatus.OCCUPIED ? Color.WHITE : new Color(35, 48, 62));
        if (Objects.equals(selectedId, s.spaceId())) b.setBorder(BorderFactory.createLineBorder(new Color(35, 112, 210), 3));
        String plate = plates.get(s.spaceId());
        b.setToolTipText("Space: " + s.spaceId() + " | Zone: " + s.zone() + " | Hall: " + s.hall() + " | Status: " + s.occupancy() + " | Operation: " + s.operation() + (plate == null ? "" : " | Plate: " + plate));
        b.addActionListener(e -> { selectedId = s.spaceId(); setSnapshotFromCurrent(); });
        return b;
    }

    private void setSnapshotFromCurrent() {
        // Repaint tab contents so the selected bay receives its highlight.
        for (Map.Entry<String, JPanel> entry : grids.entrySet()) {
            JPanel grid = entry.getValue(); grid.removeAll();
            String key = entry.getKey();
            int part = pages.getOrDefault(key, 0); String zone = key.contains(" — Part ") ? key.substring(0, key.indexOf(" — Part ")) : key;
            List<SpaceView> list = views.values().stream().filter(v -> v.zone().equals(zone)).sorted(Comparator.comparing(SpaceView::spaceId)).toList();
            int start = part * PAGE_SIZE, end = Math.min(start + PAGE_SIZE, list.size());
            for (int i = start; i < end; i++) grid.add(spaceTile(list.get(i)));
            grid.revalidate(); grid.repaint();
        }
    }

    public ParkingSpace selectedSpace() { return metadata.get(selectedId); }
    public void selectSpace(String id) { selectedId = id; setSnapshotFromCurrent(); }
    private JPanel legend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); legendItem(p, "Free", new Color(229,235,242)); legendItem(p, "Reserved / pending", new Color(255,230,153)); legendItem(p, "Occupied", new Color(150,225,184)); legendItem(p, "Disabled", new Color(83,92,104)); return p;
    }
    private void legendItem(JPanel panel, String text, Color color) { JLabel label = new JLabel("  " + text + "  "); label.setOpaque(true); label.setBackground(color); if (text.equals("Occupied") || text.equals("Disabled")) label.setForeground(Color.WHITE); panel.add(label); }
}
