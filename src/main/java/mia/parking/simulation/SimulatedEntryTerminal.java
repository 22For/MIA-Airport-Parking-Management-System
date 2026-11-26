/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.simulation;

import java.time.Clock;
import java.util.Objects;
import mia.parking.device.DeviceStatusReporter;
import mia.parking.device.EntryTerminal;
import mia.parking.domain.DeviceEventType;
import mia.parking.domain.Vehicle;
import mia.parking.dto.DeviceView;

public final class SimulatedEntryTerminal extends SimulatedDevice implements EntryTerminal {
    public SimulatedEntryTerminal(String deviceId, String locationId, Clock clock,
            DeviceStatusReporter statusReporter) {
        super(deviceId, locationId, DeviceView.DeviceType.ENTRY_TERMINAL, clock, statusReporter);
    }

    @Override public void simulateVehicleDetected(Vehicle vehicle) {
        emit(DeviceEventType.ENTRY_DETECTED, null, null, Objects.requireNonNull(vehicle, "vehicle"), null);
    }
}