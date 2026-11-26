# MIA Airport Parking Management System — shared starter

- Team member 1 / Core: Yanli Zhang (2026248006)
- Team member 2 / Devices: Zhuoyan Zhu (2026248025)
- Team member 3 / GUI: Meizhu Pan (2026248067)

## Run the entry milestone

Open pom.xml as a Maven project in IntelliJ, select JDK 25, and reload Maven dependencies.
Run mia.parking.core.EntryDemo with the repository root as its working directory.
It reads files/config/airport-config.json, prints the reserved space, simulates its sensor arrival, and prints the occupied space and parked session.
To try a different airport, pass its JSON path as the first program argument.
The loader uses Jackson. Run Maven Lifecycle → test in IntelliJ, or run mvn test from the root
(PowerShell compile.ps1 also uses Maven on PATH). The release target is Java 25.

## Airport layout and team integration

The external JSON lists hall and gate IDs, then zones (zoneId, signed floor) with groups.
Each group has a unique groupId, positive numberOfSpaces, chargingAvailable and accessible flags,
and distance maps in whole nonnegative metres covering every configured hall and gate.
Declare all fields. Keep IDs nonblank and unique within each kind; group IDs are airport-wide.
Each group generates individual IDs from its ID and a one-based, minimum-three-digit suffix.
Add a hall by adding its ID to halls and its distance to every group. Increase one count from 3 to 4
and rerun to see seven bays. In this first layout format a zone has one floor and group capabilities
apply to every generated bay. Group distances are shared; each bay has independent live state.

GUI: construct EntryParkingService as shown in EntryDemo (type it as ParkingService).
Use halls() for the destination selector, dashboard().spaces() for the grid, groupId() to group cells,
and enter(request) for assignment. SpaceView.hall() identifies the closest display hall;
allocation considers all configured destinations. Hall is now an ID record: compare with equals.
Devices: use the returned spaceId and sessionId together when constructing future sensor events.

Core first filters for enabled, free and matching capabilities, then chooses the shortest group-to-hall
distance, breaking ties by space ID. Gate distances are loaded for later routing.
Reservation, session creation and request deduplication share a single lock. A repeated request
returns the original assignment. Default duration, arrival window, currency and clock are injected;
the demo's 2 hours, 10 minutes and EUR are example values. A membership number requires a connected
MembershipService. B registers a SPACE_SENSOR at its space via reportDeviceStatus, then connects its listener with sensor.subscribe(service::onDeviceEvent). SPACE_ARRIVED requires the assigned session and bay IDs and an observation time from entry through the arrival deadline (inclusive), no later than the Core clock. Successful identical replays are ignored. The GUI receives a dashboard update after both records change. Other device events, billing,
database persistence and timeout enforcement described below belong to later milestones.

## Ownership and integration

GUI zone tabs: call `ParkingService.getZones()` for every configured zone (including empty ones),
then `getParkingSpaces(zoneId)` for its current bays. ZoneView contains zoneId and floor.
For the device monitor, give B the same EntryParkingService instance as a DeviceStatusReporter.
B calls reportDeviceStatus(DeviceView) at startup and on changes, in observation order per device.
C calls getDevices() when opening or refreshing the monitor. It returns immutable snapshots of
reported devices only, in registration order. The last report for each device ID wins.
DeviceView includes deviceId, deviceType, locationId, operationalStatus, lastSelfTestTime
(null before testing) and lastErrorMessage (null when no error is reported).
Core owns allocation, session transitions, in-memory state maps, registered-user lookup, default duration, pricing/payment policy and device-event handling. Devices owns simulations implementing device interfaces, emits immutable events, and subscribes the Core DeviceEventHandler adapter. GUI owns screens, consumes only view DTOs, calls ParkingService and marshals listener callbacks onto its UI thread (SwingUtilities.invokeLater for Swing). Shared domain/DTO/interface changes require all three members' agreement. Keep implementation packages separate: mia.parking.core, mia.parking.simulation, mia.parking.ui. Do not change interface signatures independently.

All Java files begin with the team members' names and student IDs. FIELD_REFERENCE.md is the field/nullability contract. Hall IDs come from the external layout. Currency, rounding, tariff, membership discounts, default visit duration, arrival grace and exit grace are configurable Core policies; no teacher-specific numeric values were supplied.

## Core invariants
Every accepted visit creates a ParkingSession, including occasional visitors. An occasional visitor has registeredUserId=null and no RegisteredUser is manufactured. Membership number may exist without a registered-user association. Reject invalid supplied membership according to the agreed policy; never silently register a visitor. Null requestedDuration resolves to a positive configured default in Core. Never derive billing time from entry or planned duration; bill confirmed parking occupancy according to the agreed tariff.

ParkingSpace is static configuration. ParkingSpaceState is an immutable snapshot of a mutable in-memory map exclusively owned by Core. FREE has no session, RESERVED/OCCUPIED require a session. Operational status is independent: disabling an occupied/reserved bay preserves its occupant/assignment and prevents future allocation. Match hall and all requested capabilities; choose deterministically among suitable ENABLED/FREE bays. No matching bay rejects entry without creating a partial session.

Allocate bay, reserve it, create session and record requestId in one atomic critical section. Concurrent requests must never get the same bay. Use one lock or equivalent transactional coordination, not separate check-then-write operations. Increment state version on each change. Dashboard snapshots must be mutually consistent. Publish callbacks only after commit and outside locks; isolate listener failures. Subscription.close is idempotent. All service methods may be called concurrently. Expected business failures throw ParkingException with a stable ErrorCode; argument violations may throw IllegalArgumentException.

## Lifecycle and time
Use an injected java.time.Clock in Core and Instant for stored timestamps; convert to local time only in GUI. Duration is elapsed time. Entry creates ASSIGNED with entryTime <= arrivalDeadline and reserves the bay. A valid SPACE_ARRIVED event at or before arrivalDeadline sets parkingStartTime and PARKED/OCCUPIED. SPACE_LEFT sets leaveSpaceTime, exitDeadline=leaveSpaceTime+configured grace, AWAITING_EXIT and frees the bay. Require entryTime <= parkingStartTime <= leaveSpaceTime <= exitTime when timestamps exist; exitDeadline >= leaveSpaceTime. The exit time can exceed the deadline and must still record physical exit, with an overdue alert/charge per policy. Do not erase physical observations because payment is outstanding.

Only ASSIGNED can be cancelled after its arrival deadline: release its reservation, mark CANCELLED, preserve the visit audit. PARKED must not be freed merely because planned duration elapsed. AWAITING_EXIT closes on EXIT_DETECTED and sets exitTime. Gate opening requires Core payment/deadline checks; a physical EXIT_DETECTED event is an observation, not permission to open. Terminal sessions cannot be reused. Cancelled sessions have no parking/leave/exit timestamps; ASSIGNED also has none; PARKED has parkingStartTime only; AWAITING_EXIT has parkingStartTime, leaveSpaceTime, exitDeadline; CLOSED has all timestamps. Core processes timeout checks using its clock even if no device events arrive.

## Device protocol and payment
Devices never allocate bays or mutate sessions. ENTRY_DETECTED requires vehicle; it prompts entry capture but cannot itself create a session because flight/hall/airline may be missing. SPACE_ARRIVED and SPACE_LEFT require sessionId and spaceId; EXIT_DETECTED requires sessionId; DEVICE_FAULT requires detail. Core validates configured device identity, bay binding, timestamp skew, payload and lifecycle. Deduplicate eventId and requestId: exact replay returns the prior result/no-op, conflicting reuse is rejected. Reject out-of-order events without corruption and surface diagnostics. Keep idempotency records for the session lifetime at minimum.

Use BigDecimal exclusively for money, compare with compareTo, and round explicitly using configured Currency and RoundingMode. PricingService returns nonnegative charges. Core rechecks PaymentRequest against the current quote; stale/wrong-currency/negative amounts fail without mutation. Pay is idempotent by requestId and uses a distinct paymentId. PAID is recorded only after payment succeeds. A failed payment never unlocks exit. The starter defines boundaries only; a real payment provider is outside the project scope.

## Integration acceptance checks
Core member: race two entries for one bay (exactly one succeeds); replay requests/events; verify null duration default, occasional visitor, mismatch rejection, deadlines and all lifecycle transitions. Devices member: verify start/stop/subscription cleanup and event payloads without touching Core maps. GUI member: verify immutable DTO rendering, error presentation and UI-thread callbacks. Together verify disable-while-occupied, payment failure/retry, space release before airport exit, and timestamp ordering with a fixed Clock. Compilation checks signatures only, not these behavioral obligations.

EntryDemo also sends SPACE_LEFT from the same registered sensor. Core releases that bay, preserves its operational status, and keeps the session active as AWAITING_EXIT. The observed departure time starts the exit grace period (constructor parameter; 15 minutes in the default demo). exitTime remains null until physical exit is implemented. Event replays cannot release a new reservation.
