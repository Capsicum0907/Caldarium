package io.github.capsicum0907.caldarium;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration, one block and one item per row of {@link Generator} and per
 * {@link Kind} at each {@link Tier}.
 *
 * <p>There is no list of blocks here. The loops below are the only place blocks are
 * created, and what they walk is the tables; a row added there arrives in the game,
 * in the creative tab and in every generated file without anything else being edited.
 *
 * <p>One block entity type covers every generator and one covers every kind: what a
 * machine is differs by its row, and a row is something the block already knows. A
 * type per block would be the same class registered once for each set of numbers.
 */
public final class CaldariumRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Caldarium.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Caldarium.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Caldarium.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Caldarium.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Caldarium.MODID);

    /** One menu for every machine; what it shows is decided by what opened it. */
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MACHINE_MENU =
            MENUS.register("machine", () -> IMenuTypeExtension.create(MachineMenu::new));

    private static final Map<Generator.Made, DeferredBlock<GeneratorBlock>> GENERATORS =
            new LinkedHashMap<>();
    private static final Map<Kind, Map<Tier, DeferredBlock<KindBlock>>> KINDS =
            new EnumMap<>(Kind.class);
    private static final List<DeferredItem<BlockItem>> ITEM_ORDER = new ArrayList<>();

    static {
        for (Generator.Made made : Generator.made()) {
            // Only the ones with a fire in them give light. A solar panel lit like a
            // furnace would be a lamp that works during the day.
            // ⚠ noOcclusion for a panel: a block that declares itself solid has its
            // neighbours' faces culled against it, and a three-pixel plate that did
            // that would leave a hole in the ground it sits on.
            BlockBehaviour.Properties properties = metal()
                    .lightLevel(state -> made.source().burns()
                            && state.getValue(GeneratorBlock.LIT) ? 13 : 0);
            if (made.source().flat()) {
                properties = properties.noOcclusion();
            }
            DeferredBlock<GeneratorBlock> block = BLOCKS.registerBlock(made.id(),
                    built -> new GeneratorBlock(made, built), properties);
            GENERATORS.put(made, block);
            ITEM_ORDER.add(ITEMS.registerSimpleBlockItem(block));
        }
        for (Kind kind : Kind.values()) {
            Map<Tier, DeferredBlock<KindBlock>> tiers = new LinkedHashMap<>();
            for (Tier tier : Tier.upTo(kind.top())) {
                // ⚠ noOcclusion for anything laid in lines: a block that declares
                // itself solid has its neighbours' faces culled against it, and a
                // six-pixel post that did that would leave holes around itself.
                BlockBehaviour.Properties properties =
                        kind.carries() ? metal().noOcclusion() : metal();
                DeferredBlock<KindBlock> block = BLOCKS.registerBlock(kind.id(tier),
                        built -> kind.carries()
                                ? new CarrierBlock(kind, tier, built)
                                : new KindBlock(kind, tier, built),
                        properties);
                tiers.put(tier, block);
                ITEM_ORDER.add(ITEMS.registerSimpleBlockItem(block));
            }
            KINDS.put(kind, tiers);
        }
    }

    public static final DeferredBlock<SolBlock> SOL = BLOCKS.registerBlock("sol",
            SolBlock::new, metal()
                    .strength(50.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(SolBlock::light)
                    .noOcclusion()
                    // The shape reads the config, which is not loaded when states build their caches.
                    .dynamicShape()
                    .hasPostProcess((a, b, c) -> true));

    public static final DeferredBlock<SolGlowBlock> SOL_GLOW = BLOCKS.registerBlock("sol_glow",
            SolGlowBlock::new, BlockBehaviour.Properties.of()
                    .noCollission()
                    .replaceable()
                    .instabreak()
                    .noLootTable()
                    .noOcclusion()
                    .lightLevel(SolBlock::light)
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY));

    public static final ResourceKey<DamageType> SOL_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Caldarium.MODID, "sol"));

    public static final ResourceKey<DamageType> SPOLIARIUM_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Caldarium.MODID, "spoliarium"));

    public static final DeferredItem<BlockItem> SOL_ITEM = ITEMS.registerItem("sol",
            properties -> new SolItem(SOL.get(), properties));

    static {
        ITEM_ORDER.add(SOL_ITEM);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolBlockEntity>>
            SOL_ENTITY = BLOCK_ENTITIES.register("sol",
                    () -> BlockEntityType.Builder.of(SolBlockEntity::new, SOL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>>
            GENERATOR_ENTITY = BLOCK_ENTITIES.register("generator",
                    () -> BlockEntityType.Builder.of(GeneratorBlockEntity::new,
                            blocks(GENERATORS.values())).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KindBlockEntity>>
            KIND_ENTITY = BLOCK_ENTITIES.register("kind",
                    () -> BlockEntityType.Builder.of(KindBlockEntity::new, kindBlocks()).build(null));

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
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    private static Block[] blocks(Collection<? extends DeferredBlock<? extends Block>> registered) {
        return registered.stream().map(DeferredBlock::get).toArray(Block[]::new);
    }

    private static Block[] kindBlocks() {
        List<DeferredBlock<KindBlock>> all = new ArrayList<>();
        KINDS.values().forEach(tiers -> all.addAll(tiers.values()));
        return blocks(all);
    }

    public static Map<Generator.Made, DeferredBlock<GeneratorBlock>> generators() {
        return GENERATORS;
    }

    public static DeferredBlock<KindBlock> block(Kind kind, Tier tier) {
        return KINDS.get(kind).get(tier);
    }

    /** Everything with an item, in the order it was registered. */
    public static List<DeferredItem<BlockItem>> items() {
        return ITEM_ORDER;
    }

    /**
     * A tab of its own. Eleven blocks scattered through Functional Blocks is eleven
     * blocks nobody can find; the order is the order of the tables, so the ladder
     * reads as a ladder.
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register(Caldarium.MODID, () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Caldarium.MODID))
                    .icon(() -> new ItemStack(ITEM_ORDER.getFirst().get()))
                    .displayItems((parameters, output) -> ITEM_ORDER
                            .forEach(item -> output.accept(item.get())))
                    .build());

    private CaldariumRegistry() {
    }
}
