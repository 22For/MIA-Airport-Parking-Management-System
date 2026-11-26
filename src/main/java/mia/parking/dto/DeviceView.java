/* Core: [NAME] | Devices: [NAME] | GUI: [NAME] */
package mia.parking.dto;

import java.time.Instant;
import java.util.Objects;
import mia.parking.contract.Nullable;

/** Latest full observation from a simulated device. Timestamps refer to its self-test. */
public record DeviceView(String deviceId, DeviceType deviceType, String locationId,
        Status operationalStatus, @Nullable Instant lastSelfTestTime, @Nullable String lastErrorMessage) {
    public enum DeviceType { CAMERA, SPACE_SENSOR, GATE, ENTRY_TERMINAL, EXIT_SENSOR, TICKET_PRINTER }
    public enum Status { UNKNOWN, ONLINE, OFFLINE, FAULT }

    public DeviceView {
        if (Objects.requireNonNull(deviceId).isBlank() || Objects.requireNonNull(locationId).isBlank())
            throw new IllegalArgumentException("Device and location IDs must be nonblank");
        Objects.requireNonNull(deviceType);
        Objects.requireNonNull(operationalStatus);
    }
}
