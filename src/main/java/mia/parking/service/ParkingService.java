/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
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
 /** Legacy GUI fixtures can derive tabs from bays; Core also includes configured empty zones. */
 default List<ZoneView> getZones() {
     return dashboard().spaces().stream().map(s -> new ZoneView(s.zone(), s.floor())).distinct().toList();
 }
 /** An empty or unknown zone has no bays. */
 default List<SpaceView> getParkingSpaces(String zoneId) {
     Objects.requireNonNull(zoneId);
     return dashboard().spaces().stream().filter(s -> zoneId.equals(s.zone())).toList();
 }
 default List<DeviceView> getDevices() { return List.of(); }
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
