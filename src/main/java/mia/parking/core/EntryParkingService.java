/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.core;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import mia.parking.domain.*;
import mia.parking.device.DeviceEvent;
import mia.parking.dto.*;
import mia.parking.gui.ParkingViewListener;
import mia.parking.service.*;

/** In-memory first milestone: entry, inspection and operational controls. */
public final class EntryParkingService implements ParkingService, DeviceEventHandler, mia.parking.device.DeviceStatusReporter {
    private final Map<String, DeviceView> devices = new LinkedHashMap<>();

    @Override public List<ZoneView> getZones() { return layout.zones(); }

    @Override public synchronized List<DeviceView> getDevices() { return List.copyOf(devices.values()); }

    @Override public synchronized void reportDeviceStatus(DeviceView device) {
        devices.put(device.deviceId(), device);
    }
    private record Accepted(EntryRequest request, EntryResult result) {}
    private final AirportLayout layout;
    private final Clock clock;
    private final Duration defaultDuration;
    private final Duration arrivalWindow;
    private final Currency currency;
    private final MembershipService memberships;
    private final Map<String,ParkingSpaceState> states = new LinkedHashMap<>();
    private final Map<UUID,ParkingSession> sessions = new LinkedHashMap<>();
    private final Map<UUID,Accepted> accepted = new HashMap<>();
    private final Map<UUID, DeviceEvent> confirmedArrivals = new HashMap<>();
    private final List<ParkingViewListener> listeners = new CopyOnWriteArrayList<>();

    /** Pass Optional.empty() for the standalone visitor-only demonstration. */
    public EntryParkingService(AirportLayout layout, Clock clock, Duration defaultDuration,
            Duration arrivalWindow, Currency currency, Optional<MembershipService> memberships) {
        this.layout = Objects.requireNonNull(layout);
        this.clock = Objects.requireNonNull(clock);
        this.defaultDuration = positive(defaultDuration);
        this.arrivalWindow = positive(arrivalWindow);
        this.currency = Objects.requireNonNull(currency);
        this.memberships = memberships.orElse(null);
        for (AirportLayout.Bay bay : layout.bays()) {
            String id = bay.space().spaceId();
            states.put(id, new ParkingSpaceState(id, OccupancyStatus.FREE,
                    OperationalStatus.ENABLED, null, 0, clock.instant()));
        }
    }

    private static Duration positive(Duration value) {
        if (value.isZero() || value.isNegative()) throw new IllegalArgumentException("Expected positive duration");
        return value;
    }

    public List<Hall> halls() { return layout.halls(); }

    @Override public EntryResult enter(EntryRequest request) {
        Objects.requireNonNull(request);
        EntryResult result;
        synchronized (this) {
            Accepted prior = accepted.get(request.requestId());
            if (prior != null) {
                if (!prior.request().equals(request))
                    throw new ParkingException(ErrorCode.DUPLICATE_CONFLICT, "Request ID reused with new data");
                return prior.result();
            }
            if (!halls().contains(request.hall()))
                throw new ParkingException(ErrorCode.INVALID_REQUEST, "Unknown hall: " + request.hall());
            UUID userId = null;
            if (request.membershipNumber() != null) {
                if (memberships == null || !memberships.isValid(request.membershipNumber()))
                    throw new ParkingException(ErrorCode.INVALID_REQUEST, "Membership could not be validated");
                userId = memberships.findRegisteredUser(request.membershipNumber()).map(RegisteredUser::userId).orElse(null);
            }
            // Selection and reservation share one lock: two callers cannot claim the same free bay.
            AirportLayout.Bay bay = layout.bays().stream().filter(b -> available(b, request))
                    .min(Comparator.<AirportLayout.Bay>comparingInt(b -> b.group().distancesToHalls().get(request.hall().id()))
                            .thenComparing(b -> b.space().spaceId()))
                    .orElseThrow(() -> new ParkingException(ErrorCode.NO_SPACE_AVAILABLE, "No matching enabled free space"));
            String id = bay.space().spaceId();
            Instant now = clock.instant(), deadline = now.plus(arrivalWindow);
            UUID visit = UUID.randomUUID();
            Duration planned = request.requestedDuration() == null ? defaultDuration : request.requestedDuration();
            ParkingSession session = new ParkingSession(visit, request.vehicle(), userId, request.membershipNumber(),
                    request.flightNumber(), request.hall(), request.airline(), planned, request.chargingRequired(),
                    request.accessibilityRequired(), id, SessionStatus.ASSIGNED, now, deadline, null, null, null, null);
            result = new EntryResult(visit, id, "Zone " + bay.space().zone() + ", floor " + bay.space().floor()
                    + ", group " + bay.group().groupId() + ", space " + id, deadline, planned);
            states.put(id, new ParkingSpaceState(id, OccupancyStatus.RESERVED, OperationalStatus.ENABLED,
                    visit, states.get(id).version() + 1, now));
            sessions.put(visit, session);
            accepted.put(request.requestId(), new Accepted(request, result));
        }
        publish();
        return result;
    }

    @Override public void onDeviceEvent(DeviceEvent event) {
        Objects.requireNonNull(event);
        synchronized (this) {
            DeviceEvent prior = confirmedArrivals.get(event.eventId());
            if (prior != null) {
                if (prior.equals(event)) return;
                throw new ParkingException(ErrorCode.DUPLICATE_CONFLICT, "Arrival event ID reused with different data");
            }
            if (event.type() != DeviceEventType.SPACE_ARRIVED || event.sessionId() == null
                    || event.spaceId() == null || event.spaceId().isBlank())
                throw new ParkingException(ErrorCode.INVALID_REQUEST, "Expected SPACE_ARRIVED with session and space IDs");
            ParkingSession visit = sessions.get(event.sessionId());
            if (visit == null) throw new ParkingException(ErrorCode.NOT_FOUND, "Unknown parking session");
            DeviceView sensor = devices.get(event.deviceId());
            if (sensor == null || sensor.deviceType() != DeviceView.DeviceType.SPACE_SENSOR
                    || !sensor.locationId().equals(event.spaceId()))
                throw new ParkingException(ErrorCode.INVALID_REQUEST, "Sensor is not registered for this space");
            ParkingSpaceState bay = states.get(event.spaceId());
            if (!visit.spaceId().equals(event.spaceId()) || bay == null || !visit.sessionId().equals(bay.sessionId())
                    || visit.status() != SessionStatus.ASSIGNED || bay.occupancy() != OccupancyStatus.RESERVED)
                throw new ParkingException(ErrorCode.INVALID_STATE, "Arrival does not match an active reservation");
            Instant observed = event.occurredAt();
            Instant now = clock.instant();
            if (observed.isAfter(visit.arrivalDeadline()))
                throw new ParkingException(ErrorCode.DEADLINE_EXCEEDED, "Vehicle arrived after the deadline");
            if (observed.isBefore(visit.entryTime()) || observed.isAfter(now))
                throw new ParkingException(ErrorCode.INVALID_REQUEST, "Arrival timestamp outside observation window");

            // Commit both snapshots together; a replay must not restart the parking clock.
            ParkingSession parked = new ParkingSession(visit.sessionId(), visit.vehicle(), visit.registeredUserId(),
                    visit.membershipNumber(), visit.flightNumber(), visit.hall(), visit.airline(), visit.plannedDuration(),
                    visit.chargingRequired(), visit.accessibilityRequired(), visit.spaceId(), SessionStatus.PARKED,
                    visit.entryTime(), visit.arrivalDeadline(), observed, null, null, null);
            ParkingSpaceState occupied = new ParkingSpaceState(bay.spaceId(), OccupancyStatus.OCCUPIED,
                    bay.operation(), visit.sessionId(), bay.version() + 1, now);
            sessions.put(visit.sessionId(), parked);
            states.put(bay.spaceId(), occupied);
            confirmedArrivals.put(event.eventId(), event);
        }
        publish();
    }

    private boolean available(AirportLayout.Bay bay, EntryRequest request) {
        ParkingSpace space = bay.space();
        ParkingSpaceState state = states.get(space.spaceId());
        return state.operation() == OperationalStatus.ENABLED && state.occupancy() == OccupancyStatus.FREE
                && (!request.chargingRequired() || space.chargingAvailable())
                && (!request.accessibilityRequired() || space.accessible());
    }

    @Override public synchronized Optional<SessionView> findSession(UUID id) {
        return Optional.ofNullable(sessions.get(id)).map(this::view);
    }

    private SessionView view(ParkingSession s) {
        return new SessionView(s.sessionId(), s.vehicle().plateNumber(), s.spaceId(), s.status(),
                s.entryTime(), s.arrivalDeadline(), s.parkingStartTime(), s.leaveSpaceTime(), s.exitDeadline(),
                s.exitTime(), BigDecimal.ZERO, currency);
    }

    @Override public synchronized DashboardView dashboard() {
        List<SpaceView> spaces = new ArrayList<>();
        for (AirportLayout.Bay b : layout.bays()) {
            ParkingSpace s = b.space();
            ParkingSpaceState state = states.get(s.spaceId());
            spaces.add(new SpaceView(s.spaceId(), s.hall(), s.zone(), s.floor(), s.chargingAvailable(),
                    s.accessible(), state.occupancy(), state.operation(), state.version(), b.group().groupId()));
        }
        return new DashboardView(clock.instant(), spaces, sessions.values().stream().map(this::view).toList());
    }

    @Override public void setSpaceOperation(String id, OperationalStatus operation) {
        synchronized (this) {
            ParkingSpaceState s = states.get(id);
            if (s == null) throw new ParkingException(ErrorCode.NOT_FOUND, "Unknown space: " + id);
            if (s.operation() == operation) return;
            states.put(id, new ParkingSpaceState(id, s.occupancy(), operation, s.sessionId(), s.version()+1, clock.instant()));
        }
        publish();
    }

    @Override public Subscription subscribe(ParkingViewListener listener) {
        listeners.add(Objects.requireNonNull(listener));
        return () -> listeners.remove(listener);
    }

    private void publish() {
        if (listeners.isEmpty()) return;
        DashboardView snapshot = dashboard();
        for (ParkingViewListener listener : listeners) {
            try { listener.onDashboardChanged(snapshot); }
            catch (RuntimeException ex) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Dashboard listener failed", ex);
            }
        }
    }

    @Override public PaymentResult pay(PaymentRequest request) {
        throw new ParkingException(ErrorCode.INVALID_STATE, "Payment is unavailable in the entry milestone");
    }
}
