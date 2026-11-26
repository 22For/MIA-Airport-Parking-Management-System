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
/** GUI visit projection. */
public record SessionView(UUID sessionId, String plateNumber, String spaceId, SessionStatus status, Instant entryTime, Instant arrivalDeadline, @Nullable Instant parkingStartTime, @Nullable Instant leaveSpaceTime, @Nullable Instant exitDeadline, @Nullable Instant exitTime, BigDecimal amountDue, Currency currency) {
 public SessionView {
  Objects.requireNonNull(sessionId, "sessionId");
  Objects.requireNonNull(plateNumber, "plateNumber");
  if (plateNumber.isBlank()) throw new IllegalArgumentException("plateNumber is blank");
  Objects.requireNonNull(spaceId, "spaceId");
  if (spaceId.isBlank()) throw new IllegalArgumentException("spaceId is blank");
  Objects.requireNonNull(status, "status");
  Objects.requireNonNull(entryTime, "entryTime");
  Objects.requireNonNull(arrivalDeadline, "arrivalDeadline");
  Objects.requireNonNull(amountDue, "amountDue");
  Objects.requireNonNull(currency, "currency");
 
 }
}
