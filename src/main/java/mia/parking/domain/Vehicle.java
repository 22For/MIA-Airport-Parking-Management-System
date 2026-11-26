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
/** Vehicle identity; does not imply registration. */
public record Vehicle(String plateNumber, String countryCode) {
 public Vehicle {
  Objects.requireNonNull(plateNumber, "plateNumber");
  if (plateNumber.isBlank()) throw new IllegalArgumentException("plateNumber is blank");
  Objects.requireNonNull(countryCode, "countryCode");
  if (countryCode.isBlank()) throw new IllegalArgumentException("countryCode is blank");
 
 }
}
