package io.github.capsicum0907.caldarium;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * The kinds of block whose only axis is {@link Tier}. One kind at one tier is one
 * block, the same way one row of {@link Generator} is one.
 *
 * <p>The name is built here rather than on the tier. A tier is how big something is
 * and nothing else; when it also knew how to spell "battery" there was no room for a
 * second kind that is sized the same way, which is what a charger — and later a
 * cable — is.
 *
 * <p>⚠ <b>How many slots is not a setting.</b> Everything else about these blocks is
 * in {@link CaldariumConfig}, but the number of slots decides the shape of the
 * container the server and the client each build. A number the player could change
 * between one and the other is a screen with slots in it that do not exist.
 */
public enum Kind implements StringRepresentable {
    /** Holds energy and hands it on. A row of these is a line that carries. */
    BATTERY("battery", Store.Role.BUFFER, 1, false, true),
    /** Takes energy in and puts it into what is held in it. Wider at every tier. */
    CHARGER("charger", Store.Role.SINK, 3, true, false);

    public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

    private static final int MOST_SLOTS = 9;

    private final String suffix;
    private final Store.Role role;
    private final int slots;
    private final boolean widens;
    private final boolean pushes;

    Kind(String suffix, Store.Role role, int slots, boolean widens, boolean pushes) {
        this.suffix = suffix;
        this.role = role;
        this.slots = slots;
        this.widens = widens;
        this.pushes = pushes;
    }

    /** The name in the registry, the model, the recipe and the language file. */
    public String id(Tier tier) {
        return tier.id() + "_" + suffix;
    }

    public Store.Role role() {
        return role;
    }

    /** Whether it offers what it holds to the blocks touching it. */
    public boolean pushes() {
        return pushes;
    }

    /** A row of slots, never wider than the inventory underneath it. */
    public int slots(Tier tier) {
        return widens ? Math.min(MOST_SLOTS, slots * (tier.ordinal() + 1)) : slots;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
