/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.dto;

import java.util.Objects;

public record ZoneView(String zoneId, int floor) {
    public ZoneView {
        if (Objects.requireNonNull(zoneId).isBlank()) throw new IllegalArgumentException("Blank zone ID");
    }
}
