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

    /**
     * ⚠ A switch and not a map. A map is missing a key silently, and a kind added
     * to the table would arrive as a block with no numbers at all — the same shape of
     * fault as the colour table that used to quietly repeat its last entry. This one
     * refuses to compile instead.
     *
     * <p>⚠ <b>A cable can only forward what it is holding.</b> Capacity is not a
     * comfort here, it is the throughput: a block that held less than one tick of its
     * rate would move that much per tick and the rate would be decoration. Twice the
     * rate, so a line has a tick of slack in it and does not empty and refill in step.
     */
    private static First first(Kind kind) {
        return switch (kind) {
            // A battery is capacity; a charger is a doorway, so it is quicker and
            // holds only enough to keep working while it waits for more.
            case BATTERY -> new First(50_000, 250);
            case CHARGER -> new First(12_500, 500);
            // Four times what a battery moves, and a fiftieth of what one holds.
            case CABLE, IMPORTER, EXPORTER -> new First(2_000, 1_000);
        };
    }

    /** A generator holds little: it is a source, not a store. */
    private static final int GENERATOR_CAPACITY = 5_000;
    private static final int GENERATOR_TRANSFER = 125;
    private static final int GENERATOR_PER_TICK = 5;

    /** Ten buckets, which is what a tank the size of the block ought to feel like. */
    private static final int TANK = 10_000;

    /** Most of it, so a pane of glass costs something without ruining the panel. */
    private static final int SUN_PERCENT = 60;

    /** What one block's numbers are, whatever kind of block it is. */
    public record Rates(ModConfigSpec.IntValue capacity, ModConfigSpec.IntValue transfer,
                        ModConfigSpec.IntValue perTick) {
    }

    public static final Map<Generator.Made, Rates> GENERATORS = new LinkedHashMap<>();
    private static final Map<Generator.Made, ModConfigSpec.IntValue> TANKS = new LinkedHashMap<>();

    /** How much a generator that burns a fluid can hold, in millibuckets. */
    public static int tank(Generator.Made made) {
        return TANKS.get(made).get();
    }

    /** What is left of the sun through one block that light passes through. */
    public static ModConfigSpec.IntValue SUN_THROUGH;
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
        for (Generator.Made made : Generator.made()) {
            builder.push(made.id());
            GENERATORS.put(made, new Rates(
                    builder.comment("Forge Energy it can hold before it has to stop and wait.")
                            .defineInRange("capacity", atRung(GENERATOR_CAPACITY, made.tier()),
                                    1, Integer.MAX_VALUE),
                    builder.comment("How much it offers each neighbour per tick.")
                            .defineInRange("transferRate", atRung(GENERATOR_TRANSFER, made.tier()),
                                    1, Integer.MAX_VALUE),
                    builder.comment("Forge Energy made per tick while it is working.")
                            .defineInRange("generates", atRung(GENERATOR_PER_TICK, made.tier()),
                                    1, Integer.MAX_VALUE)));
            if (made.source() == Source.FLUID) {
                TANKS.put(made, builder
                        .comment("Millibuckets of fuel it holds. A bucket is spent at a time.")
                        .defineInRange("tank", TANK, 1_000, Integer.MAX_VALUE));
            }
            builder.pop();
        }
        builder.pop();

        SUN_THROUGH = builder
                .comment("Generators that draw on daylight.")
                .push("sun")
                .comment("What percentage of the sun is left after one block that light",
                        "passes through. Anything solid overhead stops it entirely, whatever",
                        "this is set to.")
                .defineInRange("through", SUN_PERCENT, 0, 100);
        builder.pop();

        for (Kind kind : Kind.values()) {
            First first = first(kind);
            Map<Tier, Rates> tiers = new LinkedHashMap<>();
            builder.comment("One section per tier, in the order the tiers are declared.")
                    .push(kind.getSerializedName());
            for (Tier tier : Tier.upTo(kind.top())) {
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
    /**
     * The same, for something that may or may not stand on a rung at all.
     *
     * <p>⚠ <b>A machine with no rung of its own stands where iron stands.</b> Not on the
     * first rung: copper was added under iron and a burner did not get worse for it. The
     * numbers above are written for iron, and the untiered ones are the same numbers.
     */
    private static int atRung(int first, Tier tier) {
        return stepped(first, tier == null ? Tier.IRON : tier);
    }

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
