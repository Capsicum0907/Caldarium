package io.github.capsicum0907.caldarium;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * How big a thing is. The rungs of one ladder, shared by everything that has a size.
 *
 * <p>⚠ <b>The list is expected to grow.</b> Everything that reads it works from the
 * position in the list rather than from how many there are: the numbers are
 * derived in {@link CaldariumConfig}, the slots in {@link Kind}, the recipes from the
 * rung below. A rung added here needs a colour on the line and a metal in the recipe
 * provider, and nothing else.
 *
 * <p>The colour lives here rather than beside the pictures because it is identity —
 * which rung a block is on, said without a label — and because a table of colours
 * kept somewhere else is a table that can quietly run out and start repeating.
 */
public enum Tier implements StringRepresentable {
    COPPER(0xE07C57, false, 1),
    IRON(0xD5DBE0, false, 2),
    GOLD(0xF0C246, false, 2),
    DIAMOND(0x5BE0D6, false, 3),
    // ⚠ The ingot highlight rather than the block face. Netherite drawn true came out
    // at 0x5B4E52 against a 0x51565A window: a top tier that looked like a blank plate.
    NETHERITE(0xB0A2A5, false, 4),
    NETHER_STAR(0xF3EFD8, false, 6),
    COMPRESSED_NETHER_STAR(0xCBBCE8, true, 8),
    SUPER_COMPRESSED_NETHER_STAR(0x9B7BDF, true, 8);

    public static final Codec<Tier> CODEC = StringRepresentable.fromEnum(Tier::values);

    private final String id = name().toLowerCase(Locale.ROOT);
    private final int colour;
    private final boolean compressed;
    private final int step;

    Tier(int colour, boolean compressed, int step) {
        this.colour = colour;
        this.compressed = compressed;
        this.step = step;
    }

    /** How much bigger this rung is than the one under it. One on the first rung. */
    public int step() {
        return step;
    }

    /** Whether this rung is nine of the one below rather than the one below in a frame. */
    public boolean compressed() {
        return compressed;
    }

    /**
     * ⚠ A tier does not name anything. How big a thing is and what kind of thing it
     * is are two questions, and {@link Kind} answers the second one.
     */
    public String id() {
        return id;
    }

    /** The metal this rung is made of, for anything that has to look like it. */
    public int colour() {
        return colour;
    }

    /** The rungs up to and including that one; none at all for a row with no tiers. */
    public static java.util.List<Tier> upTo(Tier top) {
        if (top == null) {
            return java.util.List.of();
        }
        return java.util.List.of(values()).subList(0, top.ordinal() + 1);
    }

    /** The one below, or nothing when this is the first rung. */
    public Tier under() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
