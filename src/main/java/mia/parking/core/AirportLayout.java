/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.core;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import mia.parking.domain.*;

/** Validated static catalog. Distances in metres belong to the group, states to individual bays. */
public final class AirportLayout {
    public record Config(List<String> halls, List<String> gates, List<Zone> zones) {}
    public record Zone(String zoneId, int floor, List<Group> groups) {}
    public record Group(String groupId, int numberOfSpaces, boolean chargingAvailable,
                        boolean accessible, Map<String,Integer> distancesToHalls,
                        Map<String,Integer> distancesToGates) {
        public Group {
            distancesToHalls = Map.copyOf(distancesToHalls);
            distancesToGates = Map.copyOf(distancesToGates);
        }
    }
    public record Bay(ParkingSpace space, Group group) {}

    private final List<Hall> halls;
    private final List<mia.parking.dto.ZoneView> zoneViews;
    private final List<Bay> bays;

    public static AirportLayout load(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                    DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES,
                    DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES,
                    DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        return new AirportLayout(mapper.readValue(path.toFile(), Config.class));
    }

    private AirportLayout(Config config) {
        Set<String> hallIds = ids(config.halls(), "hall");
        Set<String> gateIds = ids(config.gates(), "gate");
        halls = config.halls().stream().map(Hall::new).toList();
        List<Bay> generated = new ArrayList<>();
        zoneViews = config.zones().stream()
                .map(z -> new mia.parking.dto.ZoneView(z.zoneId(), z.floor())).toList();
        Set<String> zones = new HashSet<>(), groups = new HashSet<>();
        for (Zone zone : config.zones()) {
            unique(zones, zone.zoneId(), "zone");
            for (Group group : zone.groups()) {
                unique(groups, group.groupId(), "group");
                if (group.numberOfSpaces() <= 0) throw new IllegalArgumentException("Invalid count: " + group.groupId());
                checkDistances(group.distancesToHalls(), hallIds, group.groupId());
                checkDistances(group.distancesToGates(), gateIds, group.groupId());
                // The existing SpaceView.hall field denotes the closest display hall.
                Hall closest = halls.stream().min(Comparator.comparingInt(h -> group.distancesToHalls().get(h.id()))).orElseThrow();
                for (int i = 0; i < group.numberOfSpaces(); i++) {
                    String id = String.format(Locale.ROOT, "%s-%03d", group.groupId(), i + 1);
                    generated.add(new Bay(new ParkingSpace(id, closest, zone.zoneId(), zone.floor(),
                            group.chargingAvailable(), group.accessible()), group));
                }
            }
        }
        bays = List.copyOf(generated);
    }

    private static Set<String> ids(List<String> values, String kind) {
        Set<String> result = new HashSet<>();
        for (String value : values) unique(result, value, kind);
        if (result.isEmpty()) throw new IllegalArgumentException("Missing " + kind);
        return result;
    }

    private static void unique(Set<String> seen, String id, String kind) {
        if (id == null || id.isBlank() || !seen.add(id))
            throw new IllegalArgumentException("Invalid or duplicate " + kind + ": " + id);
    }

    private static void checkDistances(Map<String,Integer> distances, Set<String> targets, String group) {
        if (!distances.keySet().equals(targets) || distances.values().stream().anyMatch(m -> m < 0))
            throw new IllegalArgumentException("Missing, unknown or negative distance in " + group);
    }

    public List<Hall> halls() { return halls; }
    public List<mia.parking.dto.ZoneView> zones() { return zoneViews; }
    public List<Bay> bays() { return bays; }
}
