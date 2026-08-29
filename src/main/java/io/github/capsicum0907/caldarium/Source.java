package io.github.capsicum0907.caldarium;

/**
 * What a generator draws on. The one axis {@link Generator} rows differ along.
 *
 * <p>The mod started with "a type is what it burns", which was true while every type
 * burned something. The sun does not arrive in a slot or a tank, so the question had
 * to widen to what a generator <em>draws on</em> — the same widening a kind made to
 * a tier, and for the same reason.
 */
public enum Source {
    /** Something solid, put in a slot. Whatever a furnace would burn. */
    ITEM(true),
    /** Something molten, kept in a tank. Whatever a furnace would burn by the bucket. */
    FLUID(true),
    /** Daylight, and nothing overhead. Nothing is consumed and nothing is stored. */
    SUN(false);

    private final boolean burns;

    Source(boolean burns) {
        this.burns = burns;
    }

    /** Whether there is a fire: a flame on the screen, and a light on the block. */
    public boolean burns() {
        return burns;
    }
}
