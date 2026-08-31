package io.github.capsicum0907.caldarium;

/**
 * Which neighbours a block offers what it holds to.
 *
 * <p><b>A cable keeps out of other mods.</b> One that gave to whatever it touched would
 * power a machine merely by being laid past it, and then there would be nowhere to run
 * one. So it offers to this mod's own blocks and to nothing else — which also means it
 * reaches a battery of ours directly, with no fitting in between.
 *
 * <p>⚠ It has no rule at all about being offered <em>to</em>. A generator or a battery
 * standing against a cable fills it, which is what keeps the doors optional between
 * this mod's own blocks rather than required between them.
 *
 * <p>⭐ There is no third value for the way out. A door is a cable that also does
 * something through the one face it was aimed at, and that face is the whole of the
 * difference — see {@link Aim}. When it was a wiring of its own, an exporter could
 * not talk to a cable and two of them could not talk to each other, which was true to
 * the rules and impossible to hold in your head.
 */
public enum Wiring {
    /** Outside the line, and offers to anything that will take it. */
    OPEN,
    /** Offers to this mod's own blocks and to no other mod's. */
    ALONG;

    /** Whether this block is part of the line rather than something served by one. */
    public boolean inLine() {
        return this != OPEN;
    }

    /** Whether a neighbour may be offered to. */
    public boolean mayOffer(boolean neighbourIsOurs) {
        return this == OPEN || neighbourIsOurs;
    }
}
