/* Team member 1 (Core): Yanli Zhang (2026248006)
 * Team member 2 (Devices): Zhuoyan Zhu (2026248025)
 * Team member 3 (GUI): Meizhu Pan (2026248067)
 */
package mia.parking.domain;

/** Configured hall identifier. The constants keep earlier example requests usable. */
public record Hall(String id) {
    public static final Hall NORTH = new Hall("NORTH");
    public static final Hall SOUTH = new Hall("SOUTH");

    public Hall {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Blank hall ID");
    }

    @Override public String toString() { return id; }
}
