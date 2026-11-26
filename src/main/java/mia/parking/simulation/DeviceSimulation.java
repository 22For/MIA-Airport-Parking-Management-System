/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.simulation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import mia.parking.device.DeviceEventListener;
import mia.parking.device.ParkingDevice;
import mia.parking.service.Subscription;

/** Owns device event subscriptions and starts/stops a simulation as one unit. */
public final class DeviceSimulation implements AutoCloseable {
    private final DeviceEventListener eventSink;
    private final Map<String, RegisteredDevice> devices = new LinkedHashMap<>();
    private boolean running;
    private boolean closed;

    public DeviceSimulation(DeviceEventListener eventSink) {
        this.eventSink = Objects.requireNonNull(eventSink, "eventSink");
    }

    public synchronized void register(ParkingDevice device) {
        ensureOpen();
        if (running) throw new IllegalStateException("Cannot register devices after simulation start");
        Objects.requireNonNull(device, "device");
        if (devices.containsKey(device.deviceId()))
            throw new IllegalArgumentException("Duplicate device ID: " + device.deviceId());
        Subscription subscription = device.subscribe(eventSink);
        devices.put(device.deviceId(), new RegisteredDevice(device, subscription));
    }

    public synchronized void startAll() {
        ensureOpen();
        if (running) return;
        Map<String, RegisteredDevice> started = new LinkedHashMap<>();
        try {
            for (Map.Entry<String, RegisteredDevice> entry : devices.entrySet()) {
                entry.getValue().device().start();
                started.put(entry.getKey(), entry.getValue());
            }
            running = true;
        } catch (RuntimeException exception) {
            started.values().forEach(registered -> registered.device().stop());
            throw exception;
        }
    }

    public synchronized void stopAll() {
        if (!running) return;
        RuntimeException failure = null;
        for (RegisteredDevice registered : devices.values()) {
            try {
                registered.device().stop();
            } catch (RuntimeException exception) {
                if (failure == null) failure = exception;
                else failure.addSuppressed(exception);
            }
        }
        running = false;
        if (failure != null) throw failure;
    }

    @Override public synchronized void close() {
        if (closed) return;
        RuntimeException failure = null;
        try {
            stopAll();
        } catch (RuntimeException exception) {
            failure = exception;
        }
        for (RegisteredDevice registered : devices.values()) {
            try {
                registered.subscription().close();
            } catch (RuntimeException exception) {
                if (failure == null) failure = exception;
                else failure.addSuppressed(exception);
            }
        }
        devices.clear();
        closed = true;
        if (failure != null) throw failure;
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Simulation is closed");
    }

    private record RegisteredDevice(ParkingDevice device, Subscription subscription) {}
}