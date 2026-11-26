/* Team member 1 (Core): [NAME]
 * Team member 2 (Devices): [NAME]
 * Team member 3 (GUI): [NAME]
 */
package mia.parking.gui;

import mia.parking.domain.*;
import mia.parking.dto.*;
import mia.parking.service.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** Demo-only in-memory service used to launch the GUI before Core integration. */
public final class MockParkingService implements ParkingService {
    private final List<ParkingSpace> spaces = new ArrayList<>();
    private final Map<String, OccupancyStatus> occupancy = new HashMap<>();
    private final Map<String, OperationalStatus> operation = new HashMap<>();
    private final Map<String, UUID> assignments = new HashMap<>();
    private final Map<UUID, SessionView> sessions = new LinkedHashMap<>();
    private final List<ParkingViewListener> listeners = new CopyOnWriteArrayList<>();
    private long version;

    public MockParkingService() { seedSpaces(); seedVehicles(); }

    private void seedSpaces() {
        for (int i = 1; i <= 20; i++) addSpace("A-X" + i, "A", Hall.NORTH, i == 2 || i == 8, i == 5);
        for (int i = 1; i <= 20; i++) addSpace("B-X" + i, "B", Hall.SOUTH, i == 4, false);
        occupancy.put("A-X2", OccupancyStatus.OCCUPIED); occupancy.put("A-X3", OccupancyStatus.RESERVED);
        occupancy.put("A-X8", OccupancyStatus.OCCUPIED); occupancy.put("B-X4", OccupancyStatus.RESERVED);
        operation.put("A-X5", OperationalStatus.DISABLED); operation.put("B-X12", OperationalStatus.DISABLED);
    }

    private void addSpace(String id, String zone, Hall hall, boolean charger, boolean accessible) {
        spaces.add(new ParkingSpace(id, hall, zone, 1, charger, accessible));
        occupancy.put(id, OccupancyStatus.FREE); operation.put(id, OperationalStatus.ENABLED);
    }

    private void seedVehicles() {
        Instant now = Instant.now();
        addSession("XY123", "A-X2", SessionStatus.PARKED, now.minus(Duration.ofHours(1)), now.minus(Duration.ofMinutes(55)));
        addSession("AB456", "A-X3", SessionStatus.ASSIGNED, now.minus(Duration.ofMinutes(2)), null);
        addSession("JK789", "A-X8", SessionStatus.PARKED, now.minus(Duration.ofMinutes(35)), now.minus(Duration.ofMinutes(30)));
        addSession("LM321", "B-X4", SessionStatus.ASSIGNED, now.minus(Duration.ofMinutes(1)), null);
    }

    private void addSession(String plate, String spaceId, SessionStatus status, Instant entered, Instant parked) {
        UUID id = UUID.randomUUID();
        SessionView view = new SessionView(id, plate, spaceId, status, entered, entered.plus(Duration.ofMinutes(5)), parked,
                null, null, null, BigDecimal.ZERO, Currency.getInstance("EUR"));
        sessions.put(id, view); assignments.put(spaceId, id);
    }

    @Override public synchronized DashboardView dashboard() {
        List<SpaceView> spaceViews = spaces.stream().map(s -> new SpaceView(s.spaceId(), s.hall(), s.zone(), s.floor(),
                s.chargingAvailable(), s.accessible(), occupancy.get(s.spaceId()), operation.get(s.spaceId()), version)).toList();
        return new DashboardView(Instant.now(), spaceViews, List.copyOf(sessions.values()));
    }

    @Override public void setSpaceOperation(String spaceId, OperationalStatus status) {
        DashboardView snapshot;
        synchronized (this) {
            Objects.requireNonNull(status, "status");
            if (!operation.containsKey(spaceId)) throw new ParkingException(ErrorCode.NOT_FOUND, "Parking space not found: " + spaceId);
            operation.put(spaceId, status); version++; snapshot = dashboard();
        }
        listeners.forEach(listener -> listener.onDashboardChanged(snapshot));
    }

    @Override public Subscription subscribe(ParkingViewListener listener) {
        Objects.requireNonNull(listener, "listener"); listeners.add(listener); listener.onDashboardChanged(dashboard());
        return () -> listeners.remove(listener);
    }
    @Override public Optional<SessionView> findSession(UUID sessionId) { return Optional.ofNullable(sessions.get(sessionId)); }
    @Override public EntryResult enter(EntryRequest request) { throw new UnsupportedOperationException("Entry simulation is not included in this GUI preview."); }
    @Override public PaymentResult pay(PaymentRequest request) { throw new UnsupportedOperationException("Payment simulation is not included in this GUI preview."); }
}
