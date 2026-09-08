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
    SUN(false, true),
    /** The difference between what is on one face and what is on the one opposite. */
    HEAT(false);

    private final boolean burns;
    private final boolean flat;

    Source(boolean burns) {
        this(burns, false);
    }

    Source(boolean burns, boolean flat) {
        this.burns = burns;
        this.flat = flat;
    }

    /** Whether there is a fire: a flame on the screen, and a light on the block. */
    public boolean burns() {
        return burns;
    }

    /**
     * Whether it is a panel rather than a box.
     *
     * <p>Something that draws on the sky only needs the face it points at the sky,
     * and a full cube of machinery under a solar panel is a cube that does nothing.
     * The shape, the model and the pictures all follow from this one answer.
     */
    public boolean flat() {
        return flat;
    }
}
