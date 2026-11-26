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
import mia.parking.dto.*;
import mia.parking.device.*;
import mia.parking.gui.*;
import mia.parking.service.*;

public interface SpaceSensor extends ParkingDevice {
 String spaceId();
 /** Simulation input; emits SPACE_ARRIVED or SPACE_LEFT, never edits Core state. */
 void simulatePresence(UUID sessionId, boolean present);
}
