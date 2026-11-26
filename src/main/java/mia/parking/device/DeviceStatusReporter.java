/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.device;

import mia.parking.dto.DeviceView;

/** B supplies a complete snapshot at startup and on subsequent status changes. */
@FunctionalInterface
public interface DeviceStatusReporter {
    void reportDeviceStatus(DeviceView device);
}
