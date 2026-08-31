package io.github.capsicum0907.caldarium;

/**
 * Which neighbours a block offers what it holds to.
 *
 * <p><b>There are two boundaries here, and they are not the same one.</b> Getting
 * that wrong is what made the arrangement everybody builds do nothing at all.
 *
 * <ul>
 * <li><b>The cable keeps out of other mods.</b> A cable that gave to whatever it
 *     touched would power a machine merely by being laid past it, and then there
 *     would be nowhere to run one. So it offers to this mod's own blocks and to
 *     nothing else — which also means it reaches a battery of ours directly, with no
 *     fitting in between.
 * <li><b>The exporter keeps out of the line.</b> It is the end of one, and what is at
 *     the end of a line is everything that is not the line: another mod's machine, or
 *     one of ours. An exporter that could feed a cable would be a cable.
 * </ul>
 *
 * <p>⚠ It has no rule at all about being offered <em>to</em>. A generator or a
 * battery standing against a cable fills it, which is what keeps the doors optional
 * between this mod's own blocks rather than required between them.
 */
public enum Wiring {
    /** Outside the line, and offers to anything that will take it. */
    OPEN,
    /** Offers to this mod's own blocks and to no other mod's. A cable, and the way in. */
    ALONG,
    /** Offers to everything that is not the line. The one way out of it. */
    OUT;

    /** Whether this block is part of the line rather than something served by one. */
    public boolean inLine() {
        return this != OPEN;
    }

    /**
     * Whether a neighbour may be offered to. Both facts about it are needed because
     * the two refusals are drawn in different places: one keeps out of other mods,
     * the other keeps out of the line.
     */
    public boolean mayOffer(boolean neighbourIsOurs, boolean neighbourInLine) {
        return switch (this) {
            case OPEN -> true;
            case ALONG -> neighbourIsOurs;
            case OUT -> !neighbourInLine;
        };
    }
}
