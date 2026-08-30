package io.github.capsicum0907.caldarium;

/**
 * Which neighbours a block offers what it holds to, in terms of the only boundary
 * this mod draws: its own blocks, and everybody else's.
 *
 * <p><b>The boundary is a rule about pushing and not about connecting.</b> A cable
 * refuses to <em>offer</em> to anything outside the mod, because a cable that gave to
 * whatever it touched would power a machine merely by being laid past it, and then
 * there would be nowhere to run one. It has no matching rule about being offered
 * <em>to</em>: a generator or a battery standing against a cable fills it, which is
 * what leaves the importer with the one job nothing else here can do.
 *
 * <p>⚠ <b>Drawn between the mods rather than around the cable.</b> A line closed even
 * to this mod's own batteries would need an exporter to reach one a block away, and
 * would leave the importer able to draw out of a generator that pushes anyway — two
 * blocks to bridge nothing, and a door where there is no wall. The complaint the rule
 * answers was always about somebody else's machine, so that is where it is drawn, and
 * an importer and an exporter are exactly the two doors through it.
 */
public enum Wiring {
    /** Offers to anything that will take it: a generator, a battery, a charger. */
    OPEN,
    /** Offers only to this mod's own blocks. A cable, and the way in. */
    INSIDE,
    /** Offers only to blocks that are not this mod's. The one way out. */
    OUTSIDE;

    /** Whether a neighbour on the given side of the boundary may be offered to. */
    public boolean mayOffer(boolean neighbourIsOurs) {
        return switch (this) {
            case OPEN -> true;
            case INSIDE -> neighbourIsOurs;
            case OUTSIDE -> !neighbourIsOurs;
        };
    }
}
