package io.github.capsicum0907.caldarium;

import java.util.EnumMap;
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
    private static final int PER_TIER = 8;

    /** What the first tier of each kind is worth. Every tier above is derived. */
    private record First(int capacity, int transfer) {
    }

    private static final Map<Kind, First> FIRST = Map.of(
            // A battery is capacity; a charger is a doorway, so it is quicker and
            // holds only enough to keep working while it waits for more.
            Kind.BATTERY, new First(400_000, 2_000),
            Kind.CHARGER, new First(100_000, 4_000));

    /** A generator holds little: it is a source, not a store. */
    private static final int GENERATOR_CAPACITY = 40_000;
    private static final int GENERATOR_TRANSFER = 1_000;
    private static final int GENERATOR_PER_TICK = 40;

    /** What one block's numbers are, whatever kind of block it is. */
    public record Rates(ModConfigSpec.IntValue capacity, ModConfigSpec.IntValue transfer,
                        ModConfigSpec.IntValue perTick) {
    }

    public static final Map<Generator, Rates> GENERATORS = new LinkedHashMap<>();
    private static final Map<Kind, Map<Tier, Rates>> KINDS = new EnumMap<>(Kind.class);

    /** The numbers for one block that is sized by its tier. */
    public static Rates rates(Kind kind, Tier tier) {
        return KINDS.get(kind).get(tier);
    }

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

        for (Kind kind : Kind.values()) {
            First first = FIRST.get(kind);
            Map<Tier, Rates> tiers = new LinkedHashMap<>();
            builder.comment("One section per tier, in the order the tiers are declared.")
                    .push(kind.getSerializedName());
            for (Tier tier : Tier.values()) {
                builder.push(tier.id());
                tiers.put(tier, new Rates(
                        builder.comment("Forge Energy it holds.")
                                .defineInRange("capacity", stepped(first.capacity(), tier),
                                        1, Integer.MAX_VALUE),
                        builder.comment("How much crosses its boundary per tick, each way and each side.",
                                        "Also how fast it fills what is in its slots.")
                                .defineInRange("transferRate", stepped(first.transfer(), tier),
                                        1, Integer.MAX_VALUE),
                        null));
                builder.pop();
            }
            builder.pop();
            KINDS.put(kind, tiers);
        }

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
