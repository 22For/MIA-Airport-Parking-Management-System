/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.simulation;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import mia.parking.device.DeviceStatusReporter;
import mia.parking.device.SpaceSensor;
import mia.parking.domain.DeviceEventType;
import mia.parking.dto.DeviceView;

public final class SimulatedSpaceSensor extends SimulatedDevice implements SpaceSensor {
    private final String spaceId;

    public SimulatedSpaceSensor(String deviceId, String spaceId, Clock clock,
            DeviceStatusReporter statusReporter) {
        super(deviceId, spaceId, DeviceView.DeviceType.SPACE_SENSOR, clock, statusReporter);
        this.spaceId = Objects.requireNonNull(spaceId, "spaceId");
        if (spaceId.isBlank()) throw new IllegalArgumentException("spaceId is blank");
    }

    @Override public String spaceId() { return spaceId; }

    @Override public void simulatePresence(UUID sessionId, boolean present) {
        Objects.requireNonNull(sessionId, "sessionId");
        emit(present ? DeviceEventType.SPACE_ARRIVED : DeviceEventType.SPACE_LEFT,
                sessionId, spaceId, null, null);
    }
}