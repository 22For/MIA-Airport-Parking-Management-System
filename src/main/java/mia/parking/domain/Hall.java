/* Core: [NAME] | Devices: [NAME] | GUI: [NAME] */
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
