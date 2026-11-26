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
/** Consistent read snapshot. */
public record DashboardView(Instant generatedAt, List<SpaceView> spaces, List<SessionView> activeSessions) {
 public DashboardView {
  Objects.requireNonNull(generatedAt, "generatedAt");
  Objects.requireNonNull(spaces, "spaces");
  spaces = List.copyOf(spaces);
  Objects.requireNonNull(activeSessions, "activeSessions");
  activeSessions = List.copyOf(activeSessions);
 
 }
}
