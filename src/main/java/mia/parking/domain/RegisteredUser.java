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
/** Optional persistent user record; never created for occasional visitors. */
public record RegisteredUser(UUID userId, String displayName, @Nullable String membershipNumber) {
 public RegisteredUser {
  Objects.requireNonNull(userId, "userId");
  Objects.requireNonNull(displayName, "displayName");
  if (displayName.isBlank()) throw new IllegalArgumentException("displayName is blank");
 
 }
}
