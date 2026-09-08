package io.github.capsicum0907.caldarium;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

/**
 * The table of generators. One row is one kind of generator.
 *
 * <p><b>A flat list, not a product.</b> A type of generator carries its own idea of
 * what it draws on; crossing every type with every tier would mean each type added
 * later brings a block, a model, a recipe and a name for every rung, wanted or not.
 *
 * <p>⭐ <b>A row may say that it comes in tiers, and only then is it multiplied.</b>
 * That keeps the table flat by default and still lets one row be a ladder: what the
 * sun gives is the same everywhere, so a better panel is the only way that one gets
 * better, while a burner gets better by being fed something better.
 *
 * <p>Numbers are not here — see {@link CaldariumConfig}. A row says what a generator
 * <em>is</em>; how much it makes is a setting.
 */
public record Generator(String id, Source source, Tier top) {
    /** Whatever a furnace would burn, put in a slot by hand or by a hopper. */
    public static final Generator BURNER =
            new Generator("burner", Source.ITEM, Tier.SUPER_COMPRESSED_NETHER_STAR);
    /** The same fuels, molten, kept in a tank a pipe or a bucket can fill. */
    public static final Generator CRUCIBLE =
            new Generator("crucible", Source.FLUID, Tier.SUPER_COMPRESSED_NETHER_STAR);
    /** Daylight. Nothing goes in, so the only way to get more is a better panel. */
    public static final Generator SOLAR = new Generator("solar_panel", Source.SUN, Tier.SUPER_COMPRESSED_NETHER_STAR);
    /** Hot on one face, cold on the one opposite. Placement is the whole of it. */
    public static final Generator HYPOCAUSTUM =
            new Generator("hypocaustum", Source.HEAT, Tier.SUPER_COMPRESSED_NETHER_STAR);
    /** What a player earned, poured in by hand. No better experience exists, so it tiers. */
    public static final Generator EXPERIENTIA =
            new Generator("experientia", Source.EXPERIENCE, Tier.SUPER_COMPRESSED_NETHER_STAR);

    private static final List<Generator> ALL =
            List.of(BURNER, CRUCIBLE, SOLAR, HYPOCAUSTUM, EXPERIENTIA);

    /**
     * One generator block: a row, and the rung it stands on if it stands on one.
     *
     * <p>This is what the registry, the pictures, the models, the recipes and the
     * settings all walk. A row that is not tiered appears once with no rung.
     */
    public record Made(Generator generator, Tier tier) {
        /** The name in the registry, the model, the recipe and the language file. */
        public String id() {
            return tier == null ? generator.id() : tier.id() + "_" + generator.id();
        }

        public Source source() {
            return generator.source();
        }
    }

    private static final List<Made> MADE = expand();

    private static List<Made> expand() {
        List<Made> made = new ArrayList<>();
        for (Generator generator : ALL) {
            if (generator.top == null) {
                made.add(new Made(generator, null));
                continue;
            }
            for (Tier tier : Tier.upTo(generator.top)) {
                made.add(new Made(generator, tier));
            }
        }
        return List.copyOf(made);
    }

    /** Every row. Adding a generator is adding a constant above and a name here. */
    public static List<Generator> all() {
        return ALL;
    }

    /** Every block: the rows, with the tiered ones spread over the ladder. */
    public static List<Made> made() {
        return MADE;
    }

    public static final Codec<Made> CODEC = Codec.STRING.xmap(Generator::byId, Made::id);

    /**
     * Loud on an unknown name rather than quietly handing back the first row: a save
     * naming a generator this version does not have is a fact worth seeing, and the
     * silent version turns it into a block that is subtly the wrong machine.
     */
    public static Made byId(String id) {
        for (Made made : MADE) {
            if (made.id().equals(id)) {
                return made;
            }
        }
        throw new IllegalArgumentException("No generator called " + id);
    }
}
