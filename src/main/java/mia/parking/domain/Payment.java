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
/** Payment snapshot. */
public record Payment(UUID paymentId, UUID sessionId, BigDecimal amount, Currency currency, PaymentMethod method, PaymentStatus status, @Nullable Instant paidAt, @Nullable String reference) {
 public Payment {
  Objects.requireNonNull(paymentId, "paymentId");
  Objects.requireNonNull(sessionId, "sessionId");
  Objects.requireNonNull(amount, "amount");
  Objects.requireNonNull(currency, "currency");
  Objects.requireNonNull(method, "method");
  Objects.requireNonNull(status, "status");
 if(amount.signum()<0) throw new IllegalArgumentException("negative amount");
 if((status == PaymentStatus.PAID) != (paidAt != null)) throw new IllegalArgumentException("paidAt mismatch");
 }
}
