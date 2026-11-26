/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.simulation;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import mia.parking.device.DeviceStatusReporter;
import mia.parking.device.ExitSensor;
import mia.parking.domain.DeviceEventType;
import mia.parking.dto.DeviceView;

public final class SimulatedExitSensor extends SimulatedDevice implements ExitSensor {
    public SimulatedExitSensor(String deviceId, String locationId, Clock clock,
            DeviceStatusReporter statusReporter) {
        super(deviceId, locationId, DeviceView.DeviceType.EXIT_SENSOR, clock, statusReporter);
    }

    @Override public void simulateExit(UUID sessionId) {
        emit(DeviceEventType.EXIT_DETECTED, Objects.requireNonNull(sessionId, "sessionId"), null, null, null);
    }
}