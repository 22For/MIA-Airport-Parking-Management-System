/* Team member 1 (Core): [NAME]
 * Team member 2 (Devices): [NAME]
 * Team member 3 (GUI): [NAME]
 */
package mia.parking.service;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import mia.parking.domain.*;
import mia.parking.contract.*;
/** Expected business rejection; no partial mutation is permitted. */
public final class ParkingException extends RuntimeException {
 private final ErrorCode code;
 public ParkingException(ErrorCode code, String message) { super(message); this.code = Objects.requireNonNull(code); }
 public ErrorCode code() { return code; }
}
