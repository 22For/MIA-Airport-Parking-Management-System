/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.simulation;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import mia.parking.device.DeviceEvent;
import mia.parking.device.DeviceEventListener;
import mia.parking.device.ParkingDevice;
import mia.parking.device.DeviceStatusReporter;
import mia.parking.dto.DeviceView;
import mia.parking.service.Subscription;

/** Shared lifecycle, status reporting, and event delivery for simulated devices. */
public abstract class SimulatedDevice implements ParkingDevice {
    private final String deviceId;
    private final String locationId;
    private final DeviceView.DeviceType deviceType;
    private final Clock clock;
    private final DeviceStatusReporter statusReporter;
    private final CopyOnWriteArrayList<DeviceEventListener> listeners = new CopyOnWriteArrayList<>();
    private volatile DeviceView.Status status = DeviceView.Status.UNKNOWN;
    private volatile Instant lastSelfTestTime;
    private volatile String lastErrorMessage;
    private volatile boolean running;

    protected SimulatedDevice(String deviceId, String locationId, DeviceView.DeviceType deviceType,
            Clock clock, DeviceStatusReporter statusReporter) {
        this.deviceId = required(deviceId, "deviceId");
        this.locationId = required(locationId, "locationId");
        this.deviceType = Objects.requireNonNull(deviceType, "deviceType");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.statusReporter = Objects.requireNonNull(statusReporter, "statusReporter");
    }

    private static String required(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " is blank");
        return value;
    }

    @Override public final String deviceId() { return deviceId; }

    public final String locationId() { return locationId; }

    public final DeviceView.Status operationalStatus() { return status; }

    @Override public final Subscription subscribe(DeviceEventListener listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
        return new Subscription() {
            private boolean closed;

            @Override public synchronized void close() {
                if (closed) return;
                closed = true;
                listeners.remove(listener);
            }
        };
    }

    @Override public final void start() {
        synchronized (this) {
            if (running) return;
            running = true;
            status = DeviceView.Status.ONLINE;
            lastSelfTestTime = clock.instant();
            lastErrorMessage = null;
        }
        reportStatus();
    }

    @Override public final void stop() {
        synchronized (this) {
            if (!running) return;
            running = false;
            status = DeviceView.Status.OFFLINE;
        }
        reportStatus();
    }

    /** Marks a successful simulated self-test and publishes the refreshed snapshot. */
    public final void completeSelfTest() {
        synchronized (this) {
            if (!running) throw new IllegalStateException("Device is not running");
            status = DeviceView.Status.ONLINE;
            lastSelfTestTime = clock.instant();
            lastErrorMessage = null;
        }
        reportStatus();
    }

    /** Simulates a device fault and reports its diagnostic to Core. */
    public final void reportFault(String message) {
        String error = required(message, "message");
        synchronized (this) {
            status = DeviceView.Status.FAULT;
            lastErrorMessage = error;
        }
        reportStatus();
    }

    protected final void emit(mia.parking.domain.DeviceEventType type, UUID sessionId,
            String spaceId, mia.parking.domain.Vehicle vehicle, String detail) {
        if (!running) throw new IllegalStateException("Device is not running: " + deviceId);
        DeviceEvent event = new DeviceEvent(UUID.randomUUID(), deviceId, type, clock.instant(),
                sessionId, spaceId, vehicle, detail);
        for (DeviceEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException exception) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
                        "Device event listener failed for " + deviceId, exception);
            }
        }
    }

    private void reportStatus() {
        DeviceView snapshot = new DeviceView(deviceId, deviceType, locationId, status,
                lastSelfTestTime, lastErrorMessage);
        try {
            statusReporter.reportDeviceStatus(snapshot);
        } catch (RuntimeException exception) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
                    "Device status reporter failed for " + deviceId, exception);
        }
    }
}