/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.simulation;

import java.time.Clock;
import java.util.Objects;
import mia.parking.device.DeviceStatusReporter;
import mia.parking.device.GateDevice;
import mia.parking.domain.GateCommand;
import mia.parking.dto.DeviceView;

public final class SimulatedGateDevice extends SimulatedDevice implements GateDevice {
    private volatile boolean open;

    public SimulatedGateDevice(String deviceId, String gateId, Clock clock,
            DeviceStatusReporter statusReporter) {
        super(deviceId, gateId, DeviceView.DeviceType.GATE, clock, statusReporter);
    }

    @Override public void command(GateCommand command) {
        if (operationalStatus() != DeviceView.Status.ONLINE)
            throw new IllegalStateException("Gate is not online: " + deviceId());
        open = Objects.requireNonNull(command, "command") == GateCommand.OPEN;
    }

    public boolean isOpen() { return open; }
}