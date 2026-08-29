package io.github.capsicum0907.caldarium;

import java.util.List;

import com.mojang.serialization.Codec;

/**
 * The table of generators. One row is one block.
 *
 * <p><b>A flat list, not a product.</b> {@link Tier} multiplies cleanly for things
 * whose only axis is how big they are, but a type of generator carries its own idea
 * of fuel and its own natural progression; crossing the two would mean every type
 * added later brings a block, a model, a recipe and a name for every tier, wanted or
 * not. A flat table can always be filled from a product later. A product baked into
 * the registry cannot be taken apart again.
 *
 * <p>Numbers are not here — see {@link CaldariumConfig}. A row says what a generator
 * <em>is</em>; how much it makes is a setting.
 */
public record Generator(String id, Source source) {
    /** Whatever a furnace would burn, put in a slot by hand or by a hopper. */
    public static final Generator BURNER = new Generator("burner", Source.ITEM);
    /** The same fuels, molten, kept in a tank a pipe or a bucket can fill. */
    public static final Generator CRUCIBLE = new Generator("crucible", Source.FLUID);
    /** Daylight. Nothing goes in, so there is nothing to run out of. */
    public static final Generator SOLAR = new Generator("solar_panel", Source.SUN);

    private static final List<Generator> ALL = List.of(BURNER, CRUCIBLE, SOLAR);

    public static final Codec<Generator> CODEC = Codec.STRING.xmap(Generator::byId, Generator::id);

    /** Every row. Adding a generator is adding a constant above and a name here. */
    public static List<Generator> all() {
        return ALL;
    }

    /**
     * Loud on an unknown name rather than quietly handing back the first row: a save
     * naming a generator this version does not have is a fact worth seeing, and the
     * silent version turns it into a block that is subtly the wrong machine.
     */
    public static Generator byId(String id) {
        for (Generator generator : ALL) {
            if (generator.id.equals(id)) {
                return generator;
            }
        }
        throw new IllegalArgumentException("No generator called " + id);
    }
}
