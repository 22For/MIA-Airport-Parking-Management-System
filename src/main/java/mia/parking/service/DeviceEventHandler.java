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

public interface DeviceEventHandler {
 /** Thread-safe, idempotent ingestion; reject inconsistent or stale events. */
 void onDeviceEvent(DeviceEvent event);
}
