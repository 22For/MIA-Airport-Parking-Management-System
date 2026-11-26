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
/** Core snapshot; lifecycle and timestamp consistency validated by Core. */
public record ParkingSession(UUID sessionId, Vehicle vehicle, @Nullable UUID registeredUserId, @Nullable String membershipNumber, String flightNumber, Hall hall, String airline, Duration plannedDuration, boolean chargingRequired, boolean accessibilityRequired, String spaceId, SessionStatus status, Instant entryTime, Instant arrivalDeadline, @Nullable Instant parkingStartTime, @Nullable Instant leaveSpaceTime, @Nullable Instant exitDeadline, @Nullable Instant exitTime) {
 public ParkingSession {
  Objects.requireNonNull(sessionId, "sessionId");
  Objects.requireNonNull(vehicle, "vehicle");
  Objects.requireNonNull(flightNumber, "flightNumber");
  if (flightNumber.isBlank()) throw new IllegalArgumentException("flightNumber is blank");
  Objects.requireNonNull(hall, "hall");
  Objects.requireNonNull(airline, "airline");
  if (airline.isBlank()) throw new IllegalArgumentException("airline is blank");
  Objects.requireNonNull(plannedDuration, "plannedDuration");
  Objects.requireNonNull(spaceId, "spaceId");
  if (spaceId.isBlank()) throw new IllegalArgumentException("spaceId is blank");
  Objects.requireNonNull(status, "status");
  Objects.requireNonNull(entryTime, "entryTime");
  Objects.requireNonNull(arrivalDeadline, "arrivalDeadline");
 
 }
}
