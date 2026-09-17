package io.github.capsicum0907.caldarium;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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
    private static final int FIRST_TRANSFER = 4_096;

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
            case BATTERY -> new First(128_000, FIRST_TRANSFER);
            case CHARGER -> new First(16_000, FIRST_TRANSFER);
            // Four times what a battery moves, and a fiftieth of what one holds.
            case CABLE, IMPORTER, EXPORTER -> new First(FIRST_TRANSFER * 2, FIRST_TRANSFER);
        };
    }

    /** A generator holds little: it is a source, not a store. */

    /** Ten buckets, which is what a tank the size of the block ought to feel like. */
    private static final int TANK = 10_000;

    /** Most of it, so a pane of glass costs something without ruining the panel. */
    private static final int SUN_PERCENT = 60;

    /** What one block's numbers are, whatever kind of block it is. */
    public record Rates(ModConfigSpec.IntValue capacity, ModConfigSpec.IntValue transfer,
                        ModConfigSpec.IntValue makes, ModConfigSpec.IntValue every) {
    }

    public static final Map<Generator.Made, Rates> GENERATORS = new LinkedHashMap<>();
    private static final Map<Generator.Made, ModConfigSpec.IntValue> TANKS = new LinkedHashMap<>();

    /** How much a generator that burns a fluid can hold, in millibuckets. */
    public static int tank(Generator.Made made) {
        return TANKS.get(made).get();
    }

    /** What is left of the sun through one block that light passes through. */
    public static ModConfigSpec.IntValue SUN_THROUGH;
    public static ModConfigSpec.DoubleValue HEAT_SPAN;
    public static ModConfigSpec.IntValue TICKS_PER_POINT;

    private static final int PER_POINT = 20;

    public static ModConfigSpec.IntValue TICKS_PER_HEALTH;
    public static ModConfigSpec.IntValue SOL_DURABILITY;
    public static ModConfigSpec.IntValue SOL_WEATHER;
    public static ModConfigSpec.IntValue SOL_REACH;
    public static ModConfigSpec.IntValue SOL_BURNS;
    public static ModConfigSpec.IntValue SOL_BURN_SECONDS;
    public static ModConfigSpec.DoubleValue SOL_BURN_DAMAGE;
    public static ModConfigSpec.IntValue SOL_BLAST_REACH;
    public static ModConfigSpec.DoubleValue SOL_HOLD;
    public static ModConfigSpec.DoubleValue SOL_BLAST_DAMAGE;
    public static ModConfigSpec.DoubleValue SOL_SIZE;
    public static ModConfigSpec.IntValue STORM_NATURAL;
    public static ModConfigSpec.IntValue STORM_SUMMONED;
    public static ModConfigSpec.IntValue STORM_REACH;

    private static final int MC_DAY = 24_000;
    private static final int SOL_DAYS = 30;

    private static final int PER_HEALTH = 200;
    public static ModConfigSpec.ConfigValue<List<? extends String>> TEMPERATURES;

    private static final double SPAN = 11.0;

    private static final List<String> WARMTH = List.of(
            "minecraft:lava=10.0",
            "minecraft:fire=5.0",
            "minecraft:magma_block=4.0",
            "minecraft:soul_fire=3.0",
            "minecraft:campfire=3.0",
            "minecraft:soul_campfire=2.5",
            "#minecraft:ice=-1.0",
            "minecraft:powder_snow=-1.0",
            "minecraft:snow_block=-0.5");

    private static Map<Block, Float> named;
    private static List<Map.Entry<TagKey<Block>, Float>> tagged;
    private static final Map<Kind, Map<Tier, Rates>> KINDS = new EnumMap<>(Kind.class);

    /** The numbers for one block that is sized by its tier. */
    public static Rates rates(Kind kind, Tier tier) {
        return KINDS.get(kind).get(tier);
    }

    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("generator");
        for (Generator.Made made : Generator.made()) {
            builder.push(made.id());
            GENERATORS.put(made, new Rates(
                    builder.defineInRange("capacity",
                            atRung(made.generator().capacity(), made.tier()),
                                    1, Integer.MAX_VALUE),
                    builder.defineInRange("transferRate", atRung(made.generator().transfer(), made.tier()),
                                    1, Integer.MAX_VALUE),
                    builder.defineInRange("generates",
                            atRung(made.generator().makes(), made.tier()),
                                    1, Integer.MAX_VALUE),
                    builder.defineInRange("everyTicks", made.generator().every(),
                            1, 20 * 60 * 60)));
            if (made.source() == Source.FLUID) {
                TANKS.put(made, builder
                        .defineInRange("tank", TANK, 1_000, Integer.MAX_VALUE));
            }
            builder.pop();
        }
        builder.pop();

        SUN_THROUGH = builder
                .push("sun")
                .defineInRange("through", SUN_PERCENT, 0, 100);
        builder.pop();

        builder.push("experience");
        TICKS_PER_POINT = builder.defineInRange("ticksPerPoint", PER_POINT, 1, 20_000);
        builder.pop();

        builder.push("sol");
        SOL_DURABILITY = builder.defineInRange("durability", MC_DAY * SOL_DAYS,
                1, Integer.MAX_VALUE);
        SOL_WEATHER = builder.defineInRange("weatherCost", 5, 1, 1_000);
        SOL_REACH = builder.defineInRange("reach", 1, 0, 32);
        SOL_BURNS = builder.defineInRange("burnReach", 1, 0, 32);
        SOL_BURN_SECONDS = builder.defineInRange("burnSeconds", 5, 0, 600);
        SOL_BURN_DAMAGE = builder.defineInRange("burnDamage", 2.0, 0.0, 1_000.0);
        SOL_BLAST_REACH = builder.defineInRange("blastReach", 8, 0, 64);
        SOL_HOLD = builder.defineInRange("hold", 2.0, 0.0, 16.0);
        SOL_BLAST_DAMAGE = builder.defineInRange("blastDamage", 1_000.0, 0.0, 1_000_000.0);
        SOL_SIZE = builder.defineInRange("size", 4.0, 0.5, 64.0);
        builder.pop();

        builder.push("storm");
        STORM_NATURAL = builder.defineInRange("natural", 50_000_000, 0, Integer.MAX_VALUE);
        STORM_SUMMONED = builder.defineInRange("summoned", 100_000, 0, Integer.MAX_VALUE);
        STORM_REACH = builder.defineInRange("reach", 3, 0, 16);
        builder.pop();

        builder.push("life");
        TICKS_PER_HEALTH = builder.defineInRange("ticksPerHealth", PER_HEALTH, 1, 200_000);
        builder.pop();

        builder.push("heat");
        HEAT_SPAN = builder.defineInRange("span", SPAN, 0.01, 1000.0);
        TEMPERATURES = builder.defineList("temperatures", WARMTH,
                () -> "minecraft:lava=10.0", entry -> entry instanceof String);
        builder.pop();

        for (Kind kind : Kind.values()) {
            First first = first(kind);
            Map<Tier, Rates> tiers = new LinkedHashMap<>();
            builder.push(kind.getSerializedName());
            for (Tier tier : Tier.upTo(kind.top())) {
                builder.push(tier.id());
                tiers.put(tier, new Rates(
                        builder.defineInRange("capacity", stepped(first.capacity(), tier),
                                        1, Integer.MAX_VALUE),
                        builder.defineInRange("transferRate", stepped(first.transfer(), tier),
                                        1, Integer.MAX_VALUE),
                        null, null));
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
     * ⚠ A row with no rungs is not put on one. It used to be read as standing on iron,
     * which was written when the burner had no ladder and kept its numbers where they
     * were; every generator climbs now, so the only row left without rungs was the one
     * that genuinely has none, and it was quietly getting twice what it asked for.
     */
    private static int atRung(int first, Tier tier) {
        return tier == null ? first : stepped(first, tier);
    }

    private static int stepped(int first, Tier tier) {
        long value = first;
        for (Tier rung : Tier.upTo(tier)) {
            value *= rung.step();
            if (value >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) value;
    }

    public static int stormNatural() {
        return STORM_NATURAL.get();
    }

    public static int stormSummoned() {
        return STORM_SUMMONED.get();
    }

    public static int stormReach() {
        return STORM_REACH.get();
    }

    public static int solDurability() {
        return SOL_DURABILITY.get();
    }

    public static int solWeather() {
        return SOL_WEATHER.get();
    }

    public static int solReach() {
        return SOL_REACH.get();
    }

    public static int solBurns() {
        return SOL_BURNS.get();
    }

    public static int solBurnSeconds() {
        return SOL_BURN_SECONDS.get();
    }

    public static float solSize() {
        return SOL_SIZE.get().floatValue();
    }

    public static double solHold() {
        return SOL_HOLD.get();
    }

    public static int solBlastReach() {
        return SOL_BLAST_REACH.get();
    }

    public static float solBlastDamage() {
        return SOL_BLAST_DAMAGE.get().floatValue();
    }

    public static float solBurnDamage() {
        return SOL_BURN_DAMAGE.get().floatValue();
    }

    public static int ticksPerHealth() {
        return TICKS_PER_HEALTH.get();
    }

    public static int ticksPerPoint() {
        return TICKS_PER_POINT.get();
    }

    public static float heatSpan() {
        return HEAT_SPAN.get().floatValue();
    }

    public static Float temperature(BlockState state) {
        if (named == null) {
            read();
        }
        Float given = named.get(state.getBlock());
        if (given != null) {
            return given;
        }
        for (Map.Entry<TagKey<Block>, Float> entry : tagged) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static void read() {
        named = new HashMap<>();
        tagged = new ArrayList<>();
        for (String line : TEMPERATURES.get()) {
            int split = line.lastIndexOf('=');
            if (split < 1) {
                continue;
            }
            float value;
            try {
                value = Float.parseFloat(line.substring(split + 1).trim());
            } catch (NumberFormatException wrong) {
                continue;
            }
            String what = line.substring(0, split).trim();
            if (what.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(what.substring(1));
                if (id != null) {
                    tagged.add(Map.entry(TagKey.create(BuiltInRegistries.BLOCK.key(), id), value));
                }
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(what);
            if (id != null) {
                BuiltInRegistries.BLOCK.getOptional(id)
                        .ifPresent(block -> named.put(block, value));
            }
        }
    }

    private CaldariumConfig() {
    }
}
