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
/** Entry assignment for GUI and ticket device. */
public record EntryResult(UUID sessionId, String spaceId, String directions, Instant arrivalDeadline, Duration resolvedDuration) {
 public EntryResult {
  Objects.requireNonNull(sessionId, "sessionId");
  Objects.requireNonNull(spaceId, "spaceId");
  if (spaceId.isBlank()) throw new IllegalArgumentException("spaceId is blank");
  Objects.requireNonNull(directions, "directions");
  if (directions.isBlank()) throw new IllegalArgumentException("directions is blank");
  Objects.requireNonNull(arrivalDeadline, "arrivalDeadline");
  Objects.requireNonNull(resolvedDuration, "resolvedDuration");
 
 }
}
