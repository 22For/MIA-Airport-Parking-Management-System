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

public interface EntryTerminal extends ParkingDevice {
 /** Emits ENTRY_DETECTED; complete EntryRequest is submitted via ParkingService. */
 void simulateVehicleDetected(Vehicle vehicle);
}
