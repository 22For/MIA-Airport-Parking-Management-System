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
/** GUI payment outcome. */
public record PaymentResult(UUID paymentId, PaymentStatus status, BigDecimal remainingDue, Currency currency, @Nullable String receipt) {
 public PaymentResult {
  Objects.requireNonNull(paymentId, "paymentId");
  Objects.requireNonNull(status, "status");
  Objects.requireNonNull(remainingDue, "remainingDue");
  Objects.requireNonNull(currency, "currency");
 
 }
}
