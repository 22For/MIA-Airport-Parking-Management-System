/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.simulation;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import mia.parking.device.DeviceStatusReporter;
import mia.parking.device.TicketPrinter;
import mia.parking.dto.DeviceView;
import mia.parking.dto.EntryResult;

public final class SimulatedTicketPrinter extends SimulatedDevice implements TicketPrinter {
    private volatile EntryResult lastTicket;

    public SimulatedTicketPrinter(String deviceId, String locationId, Clock clock,
            DeviceStatusReporter statusReporter) {
        super(deviceId, locationId, DeviceView.DeviceType.TICKET_PRINTER, clock, statusReporter);
    }

    @Override public void printTicket(EntryResult assignment) {
        if (operationalStatus() != DeviceView.Status.ONLINE)
            throw new IllegalStateException("Printer is not online: " + deviceId());
        lastTicket = Objects.requireNonNull(assignment, "assignment");
    }

    public Optional<EntryResult> lastTicket() { return Optional.ofNullable(lastTicket); }
}