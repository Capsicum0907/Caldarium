package io.github.capsicum0907.caldarium;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration, one block and one item per row of {@link Generator} and {@link Tier}.
 *
 * <p>There is no list of blocks here. The loops below are the only place blocks are
 * created, and what they walk is the tables; a row added there arrives in the game,
 * in the creative tab and in every generated file without anything else being edited.
 *
 * <p>One block entity type covers every generator and one covers every battery: what
 * a machine is differs by its row, and a row is something the block already knows.
 * A type per block would be the same class registered once for each set of numbers.
 */
public final class CaldariumRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Caldarium.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Caldarium.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Caldarium.MODID);

    private static final Map<Generator, DeferredBlock<GeneratorBlock>> GENERATORS = new LinkedHashMap<>();
    private static final Map<Generator, DeferredItem<BlockItem>> GENERATOR_ITEMS = new LinkedHashMap<>();
    private static final Map<Tier, DeferredBlock<BatteryBlock>> BATTERIES = new LinkedHashMap<>();
    private static final Map<Tier, DeferredItem<BlockItem>> BATTERY_ITEMS = new LinkedHashMap<>();

    static {
        for (Generator generator : Generator.all()) {
            DeferredBlock<GeneratorBlock> block = BLOCKS.registerBlock(generator.id(),
                    properties -> new GeneratorBlock(generator, properties), metal()
                            .lightLevel(state -> state.getValue(GeneratorBlock.LIT) ? 13 : 0));
            GENERATORS.put(generator, block);
            GENERATOR_ITEMS.put(generator, ITEMS.registerSimpleBlockItem(block));
        }
        for (Tier tier : Tier.values()) {
            DeferredBlock<BatteryBlock> block = BLOCKS.registerBlock(tier.batteryId(),
                    properties -> new BatteryBlock(tier, properties), metal());
            BATTERIES.put(tier, block);
            BATTERY_ITEMS.put(tier, ITEMS.registerSimpleBlockItem(block));
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>>
            GENERATOR_ENTITY = BLOCK_ENTITIES.register("generator",
                    () -> BlockEntityType.Builder.of(GeneratorBlockEntity::new, blocks(GENERATORS.values()))
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBlockEntity>>
            BATTERY_ENTITY = BLOCK_ENTITIES.register("battery",
                    () -> BlockEntityType.Builder.of(BatteryBlockEntity::new, blocks(BATTERIES.values()))
                            .build(null));

    /**
     * ⚠ A fresh Properties every time. They are mutable and carry the whole of a
     * block's behaviour, so one shared instance would make the last caller's light
     * level the light level of everything registered before it.
     *
     * <p><b>No {@code requiresCorrectToolForDrops}.</b> A battery that fails to drop
     * is a battery whose contents are gone, and being without a pickaxe should cost
     * time rather than what the block was holding.
     */
    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 6.0F)
                .sound(SoundType.METAL);
    }

    private static Block[] blocks(Collection<? extends DeferredBlock<? extends Block>> registered) {
        return registered.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }

    public static Map<Generator, DeferredBlock<GeneratorBlock>> generators() {
        return GENERATORS;
    }

    public static Map<Tier, DeferredBlock<BatteryBlock>> batteries() {
        return BATTERIES;
    }

    public static Collection<DeferredItem<BlockItem>> items() {
        return java.util.stream.Stream
                .concat(GENERATOR_ITEMS.values().stream(), BATTERY_ITEMS.values().stream())
                .toList();
    }

    private CaldariumRegistry() {
    }
}
