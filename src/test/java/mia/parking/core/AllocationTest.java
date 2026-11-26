/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.core;

import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import mia.parking.domain.*;
import mia.parking.device.DeviceEvent;
import mia.parking.gui.ParkingViewListener;
import mia.parking.dto.*;
import mia.parking.service.ParkingException;

class AllocationTest {
    // A fixed six-bay fixture keeps these assertions independent of classroom demo edits.
    private final Path config = Path.of("src/test/resources/six-bay-airport.json");

    private EntryParkingService load(Path file) throws Exception {
        return new EntryParkingService(AirportLayout.load(file), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
                Duration.ofHours(2), Duration.ofMinutes(10), Currency.getInstance("EUR"), Optional.empty());
    }

    private EntryRequest request(String hall, boolean charging) {
        return new EntryRequest(UUID.randomUUID(), new Vehicle("TEST", "US"), "T1", new Hall(hall),
                "Air", null, null, charging, true);
    }

    @Test void configuredEmptyZoneGetsItsOwnTab(@TempDir Path temp) throws Exception {
        String json = Files.readString(config).replace("\"zones\": [",
                "\"zones\": [{\"zoneId\":\"B\",\"floor\":2,\"groups\":[]},");
        var service = load(Files.writeString(temp.resolve("zones.json"), json));
        assertEquals(List.of(new ZoneView("B", 2), new ZoneView("A", 1)), service.getZones());
        assertTrue(service.getParkingSpaces("B").isEmpty());
        assertEquals(6, service.getParkingSpaces("A").size());
    }

    private DeviceEvent arrival(EntryParkingService core, EntryResult entry, String bay, Instant time) {
        String sensor = "sensor-" + bay;
        core.reportDeviceStatus(new DeviceView(sensor, DeviceView.DeviceType.SPACE_SENSOR, bay,
                DeviceView.Status.ONLINE, null, null));
        return new DeviceEvent(UUID.randomUUID(), sensor, DeviceEventType.SPACE_ARRIVED, time,
                entry.sessionId(), bay, null, null);
    }

    @Test void arrivalCommitsBothSnapshotsAndNotifiesOnceEvenOnReplay() throws Exception {
        var core = load(config);
        var entry = core.enter(request("H1", false));
        var snapshots = new ArrayList<DashboardView>();
        core.subscribe(new ParkingViewListener() {
            public void onDashboardChanged(DashboardView snapshot) { snapshots.add(snapshot); }
            public void onError(ErrorCode code, String message) { fail(message); }
        });
        var event = arrival(core, entry, entry.spaceId(), Instant.EPOCH);
        core.onDeviceEvent(event);
        core.onDeviceEvent(event);
        assertEquals(1, snapshots.size());
        var snapshot = snapshots.get(0);
        assertEquals(5, snapshot.spaces().stream().filter(s -> s.occupancy() == OccupancyStatus.FREE).count());
        assertTrue(snapshot.spaces().stream().anyMatch(s -> s.spaceId().equals(entry.spaceId())
                && s.occupancy() == OccupancyStatus.OCCUPIED && s.version() == 2));
        var parked = snapshot.activeSessions().get(0);
        assertEquals(SessionStatus.PARKED, parked.status());
        assertEquals(event.occurredAt(), parked.parkingStartTime());
        var conflict = new DeviceEvent(event.eventId(), event.deviceId(), event.type(), event.occurredAt(),
                event.sessionId(), event.spaceId(), null, "different payload");
        assertEquals(ErrorCode.DUPLICATE_CONFLICT,
                assertThrows(ParkingException.class, () -> core.onDeviceEvent(conflict)).code());
    }

    private DeviceEvent departure(DeviceEvent arrival, Instant time) {
        return new DeviceEvent(UUID.randomUUID(), arrival.deviceId(), DeviceEventType.SPACE_LEFT,
                time, arrival.sessionId(), arrival.spaceId(), null, null);
    }

    @Test void departureReleasesBayButReplayCannotReleaseNextDriversReservation() throws Exception {
        var core = new EntryParkingService(AirportLayout.load(config), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
                Duration.ofHours(2), Duration.ofMinutes(10), Duration.ofMinutes(7),
                Currency.getInstance("EUR"), Optional.empty());
        var first = core.enter(request("H1", false));
        var arrived = arrival(core, first, first.spaceId(), Instant.EPOCH);
        core.onDeviceEvent(arrived);
        var left = departure(arrived, Instant.EPOCH);
        core.onDeviceEvent(left);
        var waiting = core.findSession(first.sessionId()).orElseThrow();
        assertEquals(SessionStatus.AWAITING_EXIT, waiting.status());
        assertEquals(arrived.occurredAt(), waiting.parkingStartTime());
        assertEquals(left.occurredAt(), waiting.leaveSpaceTime());
        assertEquals(left.occurredAt().plusSeconds(420), waiting.exitDeadline());
        assertNull(waiting.exitTime());
        var secondRequest = new EntryRequest(UUID.randomUUID(), new Vehicle("SECOND", "US"), "T2",
                new Hall("H1"), "Air", null, null, false, true);
        var second = core.enter(secondRequest);
        assertEquals(first.spaceId(), second.spaceId());
        var beforeReplay = core.dashboard();
        core.onDeviceEvent(left);
        assertEquals(beforeReplay, core.dashboard());
        assertEquals(ErrorCode.INVALID_STATE, assertThrows(ParkingException.class,
                () -> core.onDeviceEvent(departure(arrived, Instant.EPOCH))).code());
        assertEquals(OccupancyStatus.RESERVED, core.dashboard().spaces().get(0).occupancy());
    }

    @Test void prematureDepartureCannotFreeTheBay() throws Exception {
        var core = load(config);
        var entry = core.enter(request("H1", false));
        var arrived = arrival(core, entry, entry.spaceId(), Instant.EPOCH);
        assertEquals(ErrorCode.INVALID_STATE, assertThrows(ParkingException.class,
                () -> core.onDeviceEvent(departure(arrived, Instant.EPOCH))).code());
        core.onDeviceEvent(arrived);
        assertEquals(ErrorCode.INVALID_REQUEST, assertThrows(ParkingException.class,
                () -> core.onDeviceEvent(departure(arrived, Instant.EPOCH.minusSeconds(1)))).code());
        assertEquals(OccupancyStatus.OCCUPIED, core.dashboard().spaces().get(0).occupancy());
    }

    @Test void wrongBayAndLateArrivalKeepReservationIntact() throws Exception {
        var core = load(config);
        var entry = core.enter(request("H1", false));
        var wrong = arrival(core, entry, "A-G01-002", Instant.EPOCH);
        assertEquals(ErrorCode.INVALID_STATE,
                assertThrows(ParkingException.class, () -> core.onDeviceEvent(wrong)).code());
        var late = arrival(core, entry, entry.spaceId(), entry.arrivalDeadline().plusNanos(1));
        assertEquals(ErrorCode.DEADLINE_EXCEEDED,
                assertThrows(ParkingException.class, () -> core.onDeviceEvent(late)).code());
        assertEquals(OccupancyStatus.RESERVED, core.dashboard().spaces().get(0).occupancy());
        assertNull(core.findSession(entry.sessionId()).orElseThrow().parkingStartTime());
    }

    @Test void deviceReportReplacesOnlyThatDeviceAndPreservesOldSnapshot() throws Exception {
        var service = load(config);
        var online = new DeviceView("S1", DeviceView.DeviceType.SPACE_SENSOR, "A-G01-001",
                DeviceView.Status.ONLINE, null, null);
        var camera = new DeviceView("C1", DeviceView.DeviceType.CAMERA, "WEST",
                DeviceView.Status.UNKNOWN, null, null);
        service.reportDeviceStatus(online);
        service.reportDeviceStatus(camera);
        var before = service.getDevices();
        var fault = new DeviceView("S1", online.deviceType(), online.locationId(),
                DeviceView.Status.FAULT, Instant.EPOCH, "Self-test failed");
        service.reportDeviceStatus(fault);
        assertEquals(List.of(fault, camera), service.getDevices());
        assertEquals(List.of(online, camera), before);
    }

    @Test void layoutChangesWithoutCode(@TempDir Path temp) throws Exception {
        String changed = Files.readString(config).replace("H1", "NEW-HALL")
                .replaceFirst("\"numberOfSpaces\": 3", "\"numberOfSpaces\": 4");
        Path file = Files.writeString(temp.resolve("airport.json"), changed);
        var service = load(file);
        assertEquals(7, service.dashboard().spaces().size());
        assertTrue(service.halls().contains(new Hall("NEW-HALL")));
        assertEquals("A-G01-001", service.enter(request("NEW-HALL", false)).spaceId());
        assertEquals("A-G02-001", service.enter(request("H2", false)).spaceId());
        Files.writeString(file, changed.replaceFirst("\"NEW-HALL\": 80", "\"TYPO\": 80"));
        assertThrows(IllegalArgumentException.class, () -> load(file));
    }

    @Test void requiresEnabledMatchingBayAndReplaysRequest() throws Exception {
        var service = load(config);
        service.setSpaceOperation("A-G02-001", OperationalStatus.DISABLED);
        var req = request("H1", true);
        var result = service.enter(req);
        assertEquals("A-G02-002", result.spaceId());
        assertEquals(result, service.enter(req));
        assertEquals(1, service.dashboard().activeSessions().size());
        assertEquals(SessionStatus.ASSIGNED, service.findSession(result.sessionId()).orElseThrow().status());
        assertEquals(Instant.EPOCH.plusSeconds(600), result.arrivalDeadline());
        assertEquals(Duration.ofHours(2), result.resolvedDuration());
    }

    @Test void concurrentEntriesCannotShareLastBay() throws Exception {
        var service = load(config);
        service.dashboard().spaces().stream().skip(1)
                .forEach(s -> service.setSpaceOperation(s.spaceId(), OperationalStatus.DISABLED));
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        Callable<Boolean> tryEnter = () -> {
            start.await();
            try { service.enter(request("H1", false)); return true; }
            catch (ParkingException ex) { assertEquals(ErrorCode.NO_SPACE_AVAILABLE, ex.code()); return false; }
        };
        try {
            var a = pool.submit(tryEnter);
            var b = pool.submit(tryEnter);
            start.countDown();
            assertNotEquals(a.get(5, TimeUnit.SECONDS), b.get(5, TimeUnit.SECONDS));
            assertEquals(1, service.dashboard().activeSessions().size());
        } finally { pool.shutdownNow(); }
    }
}
