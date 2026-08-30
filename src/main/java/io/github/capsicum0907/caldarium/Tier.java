package io.github.capsicum0907.caldarium;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * How big a thing is. The rungs of one ladder, shared by everything that has a size.
 *
 * <p>⚠ <b>The list is expected to grow.</b> Everything that reads it works from the
 * position in the list rather than from there being four of them: the numbers are
 * derived in {@link CaldariumConfig}, the slots in {@link Kind}, the recipes from the
 * rung below. A rung added here needs a colour on the line and a metal in the recipe
 * provider, and nothing else.
 *
 * <p>The colour lives here rather than beside the pictures because it is identity —
 * which rung a block is on, said without a label — and because a table of colours
 * kept somewhere else is a table that can quietly run out and start repeating.
 */
public enum Tier implements StringRepresentable {
    IRON(0xD5DBE0),
    GOLD(0xF0C246),
    DIAMOND(0x5BE0D6),
    // ⚠ The ingot highlight rather than the block face. Netherite drawn true came out
    // at 0x5B4E52 against a 0x51565A window: a top tier that looked like a blank plate.
    NETHERITE(0xB0A2A5);

    public static final Codec<Tier> CODEC = StringRepresentable.fromEnum(Tier::values);

    private final String id = name().toLowerCase(Locale.ROOT);
    private final int colour;

    Tier(int colour) {
        this.colour = colour;
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

    /** The one below, or nothing when this is the first rung. */
    public Tier under() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
