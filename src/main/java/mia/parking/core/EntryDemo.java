/* Core: [NAME] | Devices: [NAME] | GUI: [NAME] */
package mia.parking.core;

import java.nio.file.Path;
import java.time.*;
import java.util.*;
import mia.parking.domain.*;
import mia.parking.dto.*;

public final class EntryDemo {
    public static void main(String[] args) throws Exception {
        Path config = Path.of(args.length == 0 ? "files/config/airport-config.json" : args[0]);
        EntryParkingService service = new EntryParkingService(AirportLayout.load(config), Clock.systemUTC(),
                Duration.ofHours(2), Duration.ofMinutes(10), Currency.getInstance("EUR"), Optional.empty());
        System.out.println("Configured halls: " + service.halls() + ", bays: " + service.dashboard().spaces().size());
        EntryRequest request = new EntryRequest(UUID.randomUUID(), new Vehicle("DEMO01", "US"), "DEMO101",
                service.halls().get(0), "Demo Air", null, null, false, false);
        System.out.println(service.enter(request));
        service.dashboard().spaces().forEach(s -> System.out.println(s.spaceId() + " " + s.occupancy()));
    }
}
