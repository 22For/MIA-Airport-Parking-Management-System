/* Core: [NAME] | Devices: [NAME] | GUI: [NAME] */
package mia.parking.device;

import mia.parking.dto.DeviceView;

/** B supplies a complete snapshot at startup and on subsequent status changes. */
@FunctionalInterface
public interface DeviceStatusReporter {
    void reportDeviceStatus(DeviceView device);
}
