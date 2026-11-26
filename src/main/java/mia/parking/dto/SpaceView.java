/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.dto;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import mia.parking.domain.*;
import mia.parking.contract.*;
/** GUI projection without exposing mutable Core state. */
public record SpaceView(String spaceId, Hall hall, String zone, int floor, boolean chargingAvailable, boolean accessible, OccupancyStatus occupancy, OperationalStatus operation, long version, @Nullable String groupId) {
 /** Older GUI fixtures may omit group membership. Configured bays always have it. */
 public SpaceView(String spaceId, Hall hall, String zone, int floor, boolean chargingAvailable, boolean accessible,
                  OccupancyStatus occupancy, OperationalStatus operation, long version) {
     this(spaceId, hall, zone, floor, chargingAvailable, accessible, occupancy, operation, version, null);
 }
 public SpaceView {
  Objects.requireNonNull(spaceId, "spaceId");
  if (spaceId.isBlank()) throw new IllegalArgumentException("spaceId is blank");
  Objects.requireNonNull(hall, "hall");
  Objects.requireNonNull(zone, "zone");
  if (zone.isBlank()) throw new IllegalArgumentException("zone is blank");
  Objects.requireNonNull(occupancy, "occupancy");
  Objects.requireNonNull(operation, "operation");
 
 }
}
