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
import mia.parking.dto.*;
import mia.parking.device.*;
import mia.parking.gui.*;
import mia.parking.service.*;

public interface ParkingService {
 /** Configured destinations for the GUI selector; legacy mock services may override. */
 default List<Hall> halls() { return dashboard().spaces().stream().map(SpaceView::hall).distinct().toList(); }
 EntryResult enter(EntryRequest request);
 Optional<SessionView> findSession(UUID sessionId);
 DashboardView dashboard();
 PaymentResult pay(PaymentRequest request);
 /** Disable prevents new assignment; never evicts an existing session. */
 void setSpaceOperation(String spaceId, OperationalStatus operation);
 Subscription subscribe(ParkingViewListener listener);
}
