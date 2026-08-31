package io.github.capsicum0907.caldarium;

import java.util.Locale;

import com.mojang.serialization.Codec;

import io.github.capsicum0907.caldarium.data.Skins;

import net.minecraft.util.StringRepresentable;

/**
 * The kinds of block whose only axis is {@link Tier}. One kind at one tier is one
 * block, the same way one row of {@link Generator} is one.
 *
 * <p>The name is built here rather than on the tier. A tier is how big something is
 * and nothing else; when it also knew how to spell "battery" there was no room for a
 * second kind that is sized the same way, which is what a charger — and a cable — is.
 *
 * <p><b>The three that carry are rows here and nothing more.</b> A cable is a battery
 * that holds almost nothing and moves a great deal, so distance needed no mechanism
 * of its own: what it needed was a smaller capacity and a boundary, and the boundary
 * is {@link Wiring}.
 *
 * <p>⚠ <b>How many slots is not a setting.</b> Everything else about these blocks is
 * in {@link CaldariumConfig}, but the number of slots decides the shape of the
 * container the server and the client each build. A number the player could change
 * between one and the other is a screen with slots in it that do not exist.
 */
public enum Kind implements StringRepresentable {
    /** Holds energy and hands it on. A row of these is a line that carries. */
    BATTERY("battery", Store.Role.BUFFER, Wiring.OPEN, 1, false, false),
    /** Takes energy in and puts it into what is held in it. Wider at every tier. */
    CHARGER("charger", Store.Role.SINK, Wiring.OPEN, 3, true, false),
    /**
     * Distance. It offers only to this mod's own blocks, which is what lets one be
     * laid past somebody else's machine without powering it — and so what makes a
     * corridor of cable possible at all.
     */
    CABLE("cable", Store.Role.BUFFER, Wiring.INSIDE, 0, false, false),
    /**
     * The way in, and the only thing here that takes rather than gives. Everything
     * this mod makes pushes; other mods are full of machines that wait to be asked.
     *
     * <p>⚠ A {@link Store.Role#SOURCE}, so nothing can push into it. Energy that
     * went in that way would be energy in the one block whose job is to be where
     * energy starts, and the line would have two ways to move it.
     */
    IMPORTER("importer", Store.Role.SOURCE, Wiring.INSIDE, 0, false, true),
    /** The way out: the only block here that offers to another mod's machine. */
    EXPORTER("exporter", Store.Role.BUFFER, Wiring.OUTSIDE, 0, false, false);

    public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

    private static final int MOST_SLOTS = 9;

    private final String suffix;
    private final Store.Role role;
    private final Wiring wiring;
    private final int slots;
    private final boolean widens;
    private final boolean pulls;

    Kind(String suffix, Store.Role role, Wiring wiring, int slots, boolean widens,
            boolean pulls) {
        this.suffix = suffix;
        this.role = role;
        this.wiring = wiring;
        this.slots = slots;
        this.widens = widens;
        this.pulls = pulls;
    }

    /** The name in the registry, the model, the recipe and the language file. */
    public String id(Tier tier) {
        return tier.id() + "_" + suffix;
    }

    public Store.Role role() {
        return role;
    }

    public Wiring wiring() {
        return wiring;
    }

    /**
     * Whether it offers what it holds to the blocks touching it.
     *
     * <p>Derived rather than declared: anything that can give energy up does offer
     * it, and the one kind that cannot is the charger, which spends what it takes on
     * what is in its slots. A flag beside the role would be the same answer written
     * twice and free to disagree with itself.
     */
    public boolean pushes() {
        return role != Store.Role.SINK;
    }

    /** Whether it draws out of its neighbours. Only the way in does. */
    public boolean pulls() {
        return pulls;
    }

    /**
     * Whether right-clicking it opens a screen.
     *
     * <p>⭐ Nothing that carries has a window, and that is a design promise rather
     * than an omission: no wrench, because the direction is which of the three blocks
     * was placed; no upgrade to fit, because the speed is the tier; and nothing to
     * configure, so nothing to open. A block that passes the click through is also a
     * block you can build against, which matters when you are laying a hundred.
     */
    public boolean opens() {
        return wiring == Wiring.OPEN;
    }

    /** Whether it is a thing that carries rather than a thing that is served. */
    public boolean carries() {
        return wiring != Wiring.OPEN;
    }

    /**
     * Whether it is one of the two doors through the boundary. Derived: a kind that
     * draws out of another mod's machine, or offers into one, is standing in the wall.
     */
    public boolean door() {
        return pulls || wiring == Wiring.OUTSIDE;
    }

    /**
     * Whether energy can cross a face with this on one side and that on the other —
     * and so, for a thing that carries, whether it grows an arm towards it.
     *
     * <p>⭐ <b>The arm is the only thing that says so.</b> These blocks have no window
     * to open and the rule they follow refuses some neighbours on purpose, which
     * leaves nothing to tell a cable that will not talk to what it is touching from a
     * cable that is simply not carrying anything yet. An arm that grows only where
     * energy can pass makes the rule something you look at.
     */
    public boolean touches(boolean neighbourIsOurs) {
        return neighbourIsOurs || door();
    }

    /**
     * How much of the middle of the block it fills, in pixels. A whole block for
     * anything that is not laid in lines.
     */
    public int core() {
        return switch (this) {
            case BATTERY, CHARGER -> Skins.SIZE;
            case CABLE -> Skins.CORE_CABLE;
            case IMPORTER, EXPORTER -> Skins.CORE_DOOR;
        };
    }

    /**
     * A row of slots, never wider than the inventory underneath it. Two more at
     * every rung, so each tier is a width of its own rather than two of them
     * meeting the ceiling together.
     */
    public int slots(Tier tier) {
        return widens ? Math.min(MOST_SLOTS, slots + 2 * tier.ordinal()) : slots;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
