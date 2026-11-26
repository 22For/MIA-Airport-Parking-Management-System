/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import mia.parking.device.DeviceEvent;
import mia.parking.domain.DeviceEventType;
import mia.parking.domain.GateCommand;
import mia.parking.domain.Vehicle;
import mia.parking.dto.DeviceView;
import mia.parking.dto.EntryResult;

class DeviceSimulationTest {
    private static final Instant NOW = Instant.parse("2026-11-26T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void spaceSensorEmitsBoundSpaceAndSessionWithoutMutatingCore() {
        List<DeviceEvent> events = new ArrayList<>();
        List<DeviceView> reports = new ArrayList<>();
        SimulatedSpaceSensor sensor = new SimulatedSpaceSensor("sensor-1", "B-001", CLOCK, reports::add);
        sensor.subscribe(events::add);

        assertThrows(IllegalStateException.class, () -> sensor.simulatePresence(UUID.randomUUID(), true));
        sensor.start();
        UUID sessionId = UUID.randomUUID();
        sensor.simulatePresence(sessionId, true);
        sensor.simulatePresence(sessionId, false);

        assertEquals(2, events.size());
        assertEquals(DeviceEventType.SPACE_ARRIVED, events.get(0).type());
        assertEquals(DeviceEventType.SPACE_LEFT, events.get(1).type());
        assertEquals("B-001", events.get(0).spaceId());
        assertEquals(sessionId, events.get(0).sessionId());
        assertEquals(NOW, events.get(0).occurredAt());
        assertEquals(DeviceView.Status.ONLINE, reports.get(0).operationalStatus());
        assertEquals(NOW, reports.get(0).lastSelfTestTime());
    }

    @Test
    void entryAndExitDevicesEmitTheirRequiredPayloads() {
        List<DeviceEvent> events = new ArrayList<>();
        SimulatedEntryTerminal entry = new SimulatedEntryTerminal("entry-1", "gate-in", CLOCK, ignored -> {});
        SimulatedExitSensor exit = new SimulatedExitSensor("exit-1", "gate-out", CLOCK, ignored -> {});
        entry.subscribe(events::add);
        exit.subscribe(events::add);
        entry.start();
        exit.start();

        Vehicle vehicle = new Vehicle("TEST123", "US");
        UUID sessionId = UUID.randomUUID();
        entry.simulateVehicleDetected(vehicle);
        exit.simulateExit(sessionId);

        assertEquals(DeviceEventType.ENTRY_DETECTED, events.get(0).type());
        assertEquals(vehicle, events.get(0).vehicle());
        assertEquals(DeviceEventType.EXIT_DETECTED, events.get(1).type());
        assertEquals(sessionId, events.get(1).sessionId());
    }

    @Test
    void gateAndPrinterSimulateCommandsAndTickets() {
        SimulatedGateDevice gate = new SimulatedGateDevice("gate-1", "north-gate", CLOCK, ignored -> {});
        SimulatedTicketPrinter printer = new SimulatedTicketPrinter("printer-1", "north-gate", CLOCK,
                ignored -> {});
        gate.start();
        printer.start();
        gate.command(GateCommand.OPEN);

        assertTrue(gate.isOpen());
        gate.command(GateCommand.CLOSE);
        assertFalse(gate.isOpen());

        EntryResult result = new EntryResult(UUID.randomUUID(), "B-001", "Zone A", NOW,
                java.time.Duration.ofHours(2));
        printer.printTicket(result);
        assertEquals(result, printer.lastTicket().orElseThrow());
    }

    @Test
    void lifecycleReportsFaultAndOfflineSnapshotsAndIsIdempotent() {
        List<DeviceView> reports = new ArrayList<>();
        SimulatedEntryTerminal terminal = new SimulatedEntryTerminal("entry-2", "gate-in", CLOCK, reports::add);

        terminal.start();
        terminal.start();
        terminal.reportFault("Paper feed blocked");
        terminal.stop();
        terminal.stop();

        assertEquals(3, reports.size());
        assertEquals(DeviceView.Status.ONLINE, reports.get(0).operationalStatus());
        assertEquals(DeviceView.Status.FAULT, reports.get(1).operationalStatus());
        assertEquals("Paper feed blocked", reports.get(1).lastErrorMessage());
        assertEquals(DeviceView.Status.OFFLINE, reports.get(2).operationalStatus());
        assertThrows(IllegalStateException.class, terminal::completeSelfTest);
    }

    @Test
    void managerStartsStopsAndReleasesItsEventSubscription() {
        List<DeviceEvent> events = new ArrayList<>();
        SimulatedEntryTerminal terminal = new SimulatedEntryTerminal("entry-3", "gate-in", CLOCK, ignored -> {});
        DeviceSimulation simulation = new DeviceSimulation(events::add);
        simulation.register(terminal);
        simulation.startAll();
        simulation.startAll();
        terminal.simulateVehicleDetected(new Vehicle("TEST456", "US"));
        simulation.close();
        simulation.close();

        terminal.start();
        terminal.simulateVehicleDetected(new Vehicle("TEST789", "US"));
        assertEquals(1, events.size());
        assertThrows(IllegalStateException.class, simulation::startAll);
    }
}