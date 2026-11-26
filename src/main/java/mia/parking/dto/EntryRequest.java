/* Team member 1 (Core): [NAME]
 * Team member 2 (Devices): [NAME]
 * Team member 3 (GUI): [NAME]
 */
package mia.parking.dto;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import mia.parking.domain.*;
import mia.parking.contract.*;
/** Input accepted by ParkingService. */
public record EntryRequest(UUID requestId, Vehicle vehicle, String flightNumber, Hall hall, String airline, @Nullable Duration requestedDuration, @Nullable String membershipNumber, boolean chargingRequired, boolean accessibilityRequired) {
 public EntryRequest {
  Objects.requireNonNull(requestId, "requestId");
  Objects.requireNonNull(vehicle, "vehicle");
  Objects.requireNonNull(flightNumber, "flightNumber");
  if (flightNumber.isBlank()) throw new IllegalArgumentException("flightNumber is blank");
  Objects.requireNonNull(hall, "hall");
  Objects.requireNonNull(airline, "airline");
  if (airline.isBlank()) throw new IllegalArgumentException("airline is blank");
 if(requestedDuration != null && (requestedDuration.isNegative() || requestedDuration.isZero())) throw new IllegalArgumentException("duration must be positive");
 }
}
