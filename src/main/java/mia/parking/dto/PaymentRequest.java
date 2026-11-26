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
/** Payment command. */
public record PaymentRequest(UUID requestId, UUID sessionId, BigDecimal amount, Currency currency, PaymentMethod method) {
 public PaymentRequest {
  Objects.requireNonNull(requestId, "requestId");
  Objects.requireNonNull(sessionId, "sessionId");
  Objects.requireNonNull(amount, "amount");
  Objects.requireNonNull(currency, "currency");
  Objects.requireNonNull(method, "method");
 if(amount.signum()<0) throw new IllegalArgumentException("negative amount");
 }
}
