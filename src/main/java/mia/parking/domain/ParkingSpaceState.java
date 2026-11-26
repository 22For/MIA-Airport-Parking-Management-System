/* Team member 1 (Core): [NAME]
 * Team member 2 (Devices): [NAME]
 * Team member 3 (GUI): [NAME]
 */
package mia.parking.domain;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import mia.parking.domain.*;
import mia.parking.contract.*;
/** Immutable snapshot of mutable Core-owned in-memory state. */
public record ParkingSpaceState(String spaceId, OccupancyStatus occupancy, OperationalStatus operation, @Nullable UUID sessionId, long version, Instant updatedAt) {
 public ParkingSpaceState {
  Objects.requireNonNull(spaceId, "spaceId");
  if (spaceId.isBlank()) throw new IllegalArgumentException("spaceId is blank");
  Objects.requireNonNull(occupancy, "occupancy");
  Objects.requireNonNull(operation, "operation");
  Objects.requireNonNull(updatedAt, "updatedAt");
 if ((occupancy == OccupancyStatus.FREE) != (sessionId == null)) throw new IllegalArgumentException("occupancy/session mismatch");
 if(version < 0) throw new IllegalArgumentException("negative version");
 }
}
