/* Core: [NAME] | Devices: [NAME] | GUI: [NAME] */
package mia.parking.dto;

import java.util.Objects;

public record ZoneView(String zoneId, int floor) {
    public ZoneView {
        if (Objects.requireNonNull(zoneId).isBlank()) throw new IllegalArgumentException("Blank zone ID");
    }
}
