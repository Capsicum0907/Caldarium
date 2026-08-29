package io.github.capsicum0907.caldarium;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * How big a battery is. The only axis batteries have, so the enum is the table.
 *
 * <p>Identity only. What a tier holds and how fast it moves are settings, derived in
 * {@link CaldariumConfig} from the position in this list, so that a tier added here
 * arrives with numbers already in proportion to the ones below it.
 */
public enum Tier implements StringRepresentable {
    IRON;

    public static final Codec<Tier> CODEC = StringRepresentable.fromEnum(Tier::values);

    private final String id = name().toLowerCase(Locale.ROOT);

    /** The name in the registry, the model, the recipe and the language file. */
    public String batteryId() {
        return id + "_battery";
    }

    public String id() {
        return id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
