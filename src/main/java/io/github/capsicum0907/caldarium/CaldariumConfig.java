package io.github.capsicum0907.caldarium;

import java.util.LinkedHashMap;
import java.util.Map;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Every tunable value lives here. Nothing else in the mod may hold a literal.
 *
 * <p>SERVER, not COMMON: how much a battery holds is world state. A client gets the
 * host's values.
 *
 * <p>The sections are written by walking the tables rather than by hand, so a row
 * added to {@link Generator} or {@link Tier} brings its own section with it. The
 * defaults for a tier are <em>derived</em> from where it sits in the list: writing
 * them out one by one is how a ladder ends up with a rung out of proportion, and a
 * new tier would arrive with no numbers at all.
 */
public final class CaldariumConfig {
    /**
     * The proportions the ladder is built from — the only numbers in the mod, and
     * only ever the starting point of a file the player then owns.
     */
    private static final int FIRST_CAPACITY = 400_000;
    private static final int FIRST_TRANSFER = 2_000;
    private static final int PER_TIER = 8;

    /** A generator holds little: it is a source, not a store. */
    private static final int GENERATOR_CAPACITY = 40_000;
    private static final int GENERATOR_TRANSFER = 1_000;
    private static final int GENERATOR_PER_TICK = 40;

    /** What one block's numbers are, whatever kind of block it is. */
    public record Rates(ModConfigSpec.IntValue capacity, ModConfigSpec.IntValue transfer,
                        ModConfigSpec.IntValue perTick) {
    }

    public static final Map<Generator, Rates> GENERATORS = new LinkedHashMap<>();
    public static final Map<Tier, Rates> BATTERIES = new LinkedHashMap<>();

    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Generators, a section each. A type is what it burns; these are its numbers.")
                .push("generator");
        for (Generator generator : Generator.all()) {
            builder.push(generator.id());
            GENERATORS.put(generator, new Rates(
                    builder.comment("Forge Energy it can hold before it has to stop and wait.")
                            .defineInRange("capacity", GENERATOR_CAPACITY, 1, Integer.MAX_VALUE),
                    builder.comment("How much it offers each neighbour per tick.")
                            .defineInRange("transferRate", GENERATOR_TRANSFER, 1, Integer.MAX_VALUE),
                    builder.comment("Forge Energy made per tick while it is burning.")
                            .defineInRange("generates", GENERATOR_PER_TICK, 1, Integer.MAX_VALUE)));
            builder.pop();
        }
        builder.pop();

        builder.comment("Batteries, a section each, in the order the tiers are declared.")
                .push("battery");
        for (Tier tier : Tier.values()) {
            builder.push(tier.id());
            BATTERIES.put(tier, new Rates(
                    builder.comment("Forge Energy it holds.")
                            .defineInRange("capacity", stepped(FIRST_CAPACITY, tier), 1, Integer.MAX_VALUE),
                    builder.comment("How much crosses its boundary per tick, each way and each side.")
                            .defineInRange("transferRate", stepped(FIRST_TRANSFER, tier), 1, Integer.MAX_VALUE),
                    null));
            builder.pop();
        }
        builder.pop();

        SPEC = builder.build();
    }

    /**
     * The first tier's number, multiplied once per rung climbed, and clamped: the
     * energy interface counts in ints, so a ladder tall enough will meet the ceiling
     * whatever the numbers are. Meeting it is not an error — it is the largest
     * battery that can honestly report itself.
     */
    private static int stepped(int first, Tier tier) {
        long value = first;
        for (int rung = 0; rung < tier.ordinal(); rung++) {
            value *= PER_TIER;
            if (value >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) value;
    }

    private CaldariumConfig() {
    }
}
