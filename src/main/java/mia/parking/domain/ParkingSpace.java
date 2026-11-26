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
/** Static configuration; contains no occupancy or enablement state. */
public record ParkingSpace(String spaceId, Hall hall, String zone, int floor, boolean chargingAvailable, boolean accessible) {
 public ParkingSpace {
  Objects.requireNonNull(spaceId, "spaceId");
  if (spaceId.isBlank()) throw new IllegalArgumentException("spaceId is blank");
  Objects.requireNonNull(hall, "hall");
  Objects.requireNonNull(zone, "zone");
  if (zone.isBlank()) throw new IllegalArgumentException("zone is blank");
 
 }
}
