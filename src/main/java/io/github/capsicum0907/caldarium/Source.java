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
    ITEM(true, false, true),
    /** Something molten, kept in a tank. Whatever a furnace would burn by the bucket. */
    FLUID(true, false, true),
    /** Daylight, and nothing overhead. Nothing is consumed and nothing is stored. */
    SUN(false, true, false),
    /** The difference between what is on one face and what is on the one opposite. */
    HEAT(false, false, false),
    /** What a player has earned, poured in by hand and paid for at once. */
    EXPERIENCE(false, false, false),
    /** The health of whatever stands on it, and only what actually left. */
    LIFE(false, false, false),
    /** Whatever light is falling on it, from lamps rather than from the sky. */
    LAMP(false, true, false),
    /** Lightning, which arrives all at once or not for an hour. */
    STORM(false, false, false),
    /** Being hit. What is measured is the blow, and the blow is somebody's work. */
    BLOW(false, false, false);

    private final boolean burns;
    private final boolean flat;
    private final boolean stored;

    Source(boolean burns, boolean flat, boolean stored) {
        this.burns = burns;
        this.flat = flat;
        this.stored = stored;
    }

    /** Whether it holds what it draws on and spends it over time. */
    public boolean stored() {
        return stored;
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
