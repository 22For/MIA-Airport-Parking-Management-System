/* Team member 1 (Core): [NAME]
 * Team member 2 (Devices): [NAME]
 * Team member 3 (GUI): [NAME]
 */
package mia.parking.device;

import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import mia.parking.domain.*;
import mia.parking.contract.*;
/** Immutable event; conditional payload rules validated by Core. */
public record DeviceEvent(UUID eventId, String deviceId, DeviceEventType type, Instant occurredAt, @Nullable UUID sessionId, @Nullable String spaceId, @Nullable Vehicle vehicle, @Nullable String detail) {
 public DeviceEvent {
  Objects.requireNonNull(eventId, "eventId");
  Objects.requireNonNull(deviceId, "deviceId");
  if (deviceId.isBlank()) throw new IllegalArgumentException("deviceId is blank");
  Objects.requireNonNull(type, "type");
  Objects.requireNonNull(occurredAt, "occurredAt");
 
 }
}
