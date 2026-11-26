# Field reference

All references are non-null unless annotated `@Nullable`. Blank required strings and null list elements are rejected. Records are immutable snapshots. Conditional lifecycle rules are Core responsibilities, not automatic record validation.

| Field | Java type | Nullable | Contract |
|---|---|---|---|
| `Vehicle.plateNumber` | `String` | No | Canonical uppercase, whitespace removed by Core |
| `Vehicle.countryCode` | `String` | No | ISO country code |
| `RegisteredUser.userId` | `UUID` | No | Stable registered user ID |
| `RegisteredUser.displayName` | `String` | No | Nonblank name |
| `RegisteredUser.membershipNumber` | `String` | Yes | Unique when supplied |
| `ParkingSpace.spaceId` | `String` | No | Unique stable ID |
| `ParkingSpace.hall` | `Hall` | No | Hall served |
| `ParkingSpace.zone` | `String` | No | Signage zone |
| `ParkingSpace.floor` | `int` | No | Signed floor number |
| `ParkingSpace.chargingAvailable` | `boolean` | No | Charger capability |
| `ParkingSpace.accessible` | `boolean` | No | Accessible bay capability |
| `ParkingSpaceState.spaceId` | `String` | No | References ParkingSpace |
| `ParkingSpaceState.occupancy` | `OccupancyStatus` | No | FREE iff sessionId is null |
| `ParkingSpaceState.operation` | `OperationalStatus` | No | Independent of occupancy |
| `ParkingSpaceState.sessionId` | `UUID` | Yes | Required for RESERVED or OCCUPIED |
| `ParkingSpaceState.version` | `long` | No | Nonnegative optimistic version |
| `ParkingSpaceState.updatedAt` | `Instant` | No | Core clock time |
| `ParkingSession.sessionId` | `UUID` | No | One ID for every visit including occasional visitors |
| `ParkingSession.vehicle` | `Vehicle` | No | Vehicle visiting |
| `ParkingSession.registeredUserId` | `UUID` | Yes | Null for occasional visitors |
| `ParkingSession.membershipNumber` | `String` | Yes | Validated membership if provided; independent of user registration |
| `ParkingSession.flightNumber` | `String` | No | Flight supplied at entry |
| `ParkingSession.hall` | `Hall` | No | Requested hall |
| `ParkingSession.airline` | `String` | No | Airline supplied at entry |
| `ParkingSession.plannedDuration` | `Duration` | No | Positive resolved duration; never null |
| `ParkingSession.chargingRequired` | `boolean` | No | Entry requirement |
| `ParkingSession.accessibilityRequired` | `boolean` | No | Entry requirement |
| `ParkingSession.spaceId` | `String` | No | Assigned bay |
| `ParkingSession.status` | `SessionStatus` | No | Lifecycle defined in README |
| `ParkingSession.entryTime` | `Instant` | No | Accepted entry time |
| `ParkingSession.arrivalDeadline` | `Instant` | No | Deadline to reach assigned bay |
| `ParkingSession.parkingStartTime` | `Instant` | Yes | Set only on confirmed space arrival |
| `ParkingSession.leaveSpaceTime` | `Instant` | Yes | Set on confirmed space departure |
| `ParkingSession.exitDeadline` | `Instant` | Yes | Set together with leaveSpaceTime |
| `ParkingSession.exitTime` | `Instant` | Yes | Set on confirmed airport exit |
| `Payment.paymentId` | `UUID` | No | Stable ID |
| `Payment.sessionId` | `UUID` | No | Charged visit |
| `Payment.amount` | `BigDecimal` | No | Nonnegative; currency scale via Core rounding policy |
| `Payment.currency` | `Currency` | No | Configured currency |
| `Payment.method` | `PaymentMethod` | No | Payment channel |
| `Payment.status` | `PaymentStatus` | No | PAID only after successful confirmation |
| `Payment.paidAt` | `Instant` | Yes | Present iff PAID |
| `Payment.reference` | `String` | Yes | Provider or receipt reference |
| `EntryRequest.requestId` | `UUID` | No | Idempotency key |
| `EntryRequest.vehicle` | `Vehicle` | No | Required vehicle |
| `EntryRequest.flightNumber` | `String` | No | Required flight |
| `EntryRequest.hall` | `Hall` | No | Required hall |
| `EntryRequest.airline` | `String` | No | Required airline |
| `EntryRequest.requestedDuration` | `Duration` | Yes | Null means Core configured default; otherwise positive |
| `EntryRequest.membershipNumber` | `String` | Yes | Optional; Core validates eligibility |
| `EntryRequest.chargingRequired` | `boolean` | No | Only matching spaces |
| `EntryRequest.accessibilityRequired` | `boolean` | No | Only matching spaces |
| `EntryResult.sessionId` | `UUID` | No | Created visit ID |
| `EntryResult.spaceId` | `String` | No | Allocated bay |
| `EntryResult.directions` | `String` | No | Human-readable route |
| `EntryResult.arrivalDeadline` | `Instant` | No | Reach bay by this instant |
| `EntryResult.resolvedDuration` | `Duration` | No | Core default or requested duration |
| `SpaceView.spaceId` | `String` | No | Bay label |
| `SpaceView.hall` | `Hall` | No | Hall |
| `SpaceView.zone` | `String` | No | Zone |
| `SpaceView.floor` | `int` | No | Floor |
| `SpaceView.chargingAvailable` | `boolean` | No | Charger capability |
| `SpaceView.accessible` | `boolean` | No | Accessibility capability |
| `SpaceView.occupancy` | `OccupancyStatus` | No | Live occupancy |
| `SpaceView.operation` | `OperationalStatus` | No | Enablement |
| `SpaceView.version` | `long` | No | State version |
| `SessionView.sessionId` | `UUID` | No | Visit ID |
| `SessionView.plateNumber` | `String` | No | Display plate |
| `SessionView.spaceId` | `String` | No | Assigned bay |
| `SessionView.status` | `SessionStatus` | No | Visit status |
| `SessionView.entryTime` | `Instant` | No | Entry |
| `SessionView.arrivalDeadline` | `Instant` | No | Arrival deadline |
| `SessionView.parkingStartTime` | `Instant` | Yes | Confirmed arrival |
| `SessionView.leaveSpaceTime` | `Instant` | Yes | Confirmed departure |
| `SessionView.exitDeadline` | `Instant` | Yes | Exit deadline |
| `SessionView.exitTime` | `Instant` | Yes | Exit |
| `SessionView.amountDue` | `BigDecimal` | No | Nonnegative current balance |
| `SessionView.currency` | `Currency` | No | Balance currency |
| `DashboardView.generatedAt` | `Instant` | No | Snapshot time |
| `DashboardView.spaces` | `List<SpaceView>` | No | Immutable complete space list |
| `DashboardView.activeSessions` | `List<SessionView>` | No | Immutable nonterminal visit list |
| `PaymentRequest.requestId` | `UUID` | No | Idempotency key |
| `PaymentRequest.sessionId` | `UUID` | No | Visit to pay |
| `PaymentRequest.amount` | `BigDecimal` | No | Exact quoted amount; Core rechecks |
| `PaymentRequest.currency` | `Currency` | No | Must match quote |
| `PaymentRequest.method` | `PaymentMethod` | No | Selected channel |
| `PaymentResult.paymentId` | `UUID` | No | Payment ID |
| `PaymentResult.status` | `PaymentStatus` | No | Outcome |
| `PaymentResult.remainingDue` | `BigDecimal` | No | Balance after operation |
| `PaymentResult.currency` | `Currency` | No | Currency |
| `PaymentResult.receipt` | `String` | Yes | Receipt only on success |
| `DeviceEvent.eventId` | `UUID` | No | Deduplication key |
| `DeviceEvent.deviceId` | `String` | No | Configured emitting device |
| `DeviceEvent.type` | `DeviceEventType` | No | Event discriminator |
| `DeviceEvent.occurredAt` | `Instant` | No | Device observation; Core checks clock skew |
| `DeviceEvent.sessionId` | `UUID` | Yes | Required for arrival, departure and exit |
| `DeviceEvent.spaceId` | `String` | Yes | Required for space arrival/departure |
| `DeviceEvent.vehicle` | `Vehicle` | Yes | Required for entry detection |
| `DeviceEvent.detail` | `String` | Yes | Required for device fault; optional diagnostic otherwise |

Entry milestone additions: Hall.id is a nonblank configured identifier. SpaceView.groupId is populated for all configured bays and may be null in older GUI fixtures. ParkingSpace.hall and SpaceView.hall describe the closest hall; EntryRequest.hall and ParkingSession.hall describe the requested destination. Group distance maps use whole metres. See README for the airport JSON schema.
