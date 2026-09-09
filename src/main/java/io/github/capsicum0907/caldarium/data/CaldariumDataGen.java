package io.github.capsicum0907.caldarium.data;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.common.hash.Hashing;

import io.github.capsicum0907.caldarium.Caldarium;
import io.github.capsicum0907.caldarium.CaldariumRegistry;
import io.github.capsicum0907.caldarium.Generator;
import io.github.capsicum0907.caldarium.CarrierBlock;
import io.github.capsicum0907.caldarium.Joint;
import io.github.capsicum0907.caldarium.Kind;
import io.github.capsicum0907.caldarium.GeneratorBlock;
import io.github.capsicum0907.caldarium.SolBlock;
import io.github.capsicum0907.caldarium.Tier;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Everything under {@code src/generated/resources} comes from here, so nothing in
 * that directory is written by hand - the textures included.
 *
 * <p>Every provider walks the tables rather than a list of its own. A row added to
 * {@link Generator} or {@link Tier} brings its block, its picture, its model, its
 * drop, its recipe and its name with it.
 */
@EventBusSubscriber(modid = Caldarium.MODID, value = { Dist.CLIENT, Dist.DEDICATED_SERVER })
public final class CaldariumDataGen {
    private CaldariumDataGen() {
    }

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        // The model provider refuses to name a texture it cannot find, and these are
        // written in this same run, so it is told in advance what is about to exist.
        ExistingFileHelper helper = event.getExistingFileHelper();
        ExistingFileHelper.ResourceType texture =
                new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");
        for (String skin : Skins.names()) {
            helper.trackGenerated(
                    ResourceLocation.fromNamespaceAndPath(Caldarium.MODID, "block/" + skin), texture);
        }

        generator.addProvider(event.includeClient(), new Textures(output));
        generator.addProvider(event.includeClient(), new Models(output, helper));
        generator.addProvider(event.includeClient(), new Language(output));

        generator.addProvider(event.includeServer(), new Loot(output, lookup));
        generator.addProvider(event.includeServer(), new Recipes(output, lookup));
        generator.addProvider(event.includeServer(), new TestStructures(output));
        generator.addProvider(event.includeServer(),
                new Tags(output, lookup, helper));
    }

    /** The pictures, from {@link Skins}. */
    private static class Textures implements DataProvider {
        private final PackOutput.PathProvider textures;
        private final PackOutput.PathProvider screens;

        Textures(PackOutput output) {
            this.textures = output.createPathProvider(PackOutput.Target.RESOURCE_PACK,
                    "textures/block");
            this.screens = output.createPathProvider(PackOutput.Target.RESOURCE_PACK,
                    "textures/gui");
        }

        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            List<CompletableFuture<?>> writing = new ArrayList<>();
            draw(output, writing, Skins.solSkin(0.0F), Skins.SOL);
            draw(output, writing, Skins.litSkin(), Skins.SOL_LIT);
            for (Generator.Made made : Generator.made()) {
                if (made.source().flat()) {
                    for (boolean lit : new boolean[] { false, true }) {
                        draw(output, writing, Skins.solarSkin(made.tier(), lit),
                                Skins.generatorTop(made, lit));
                    }
                    draw(output, writing, Skins.plainSkin(made.tier()),
                            Skins.generatorSide(made));
                    continue;
                }
                for (boolean lit : new boolean[] { false, true }) {
                    draw(output, writing, Skins.generatorSkin(made, lit),
                            Skins.generator(made, lit));
                }
            }
            for (Kind kind : Kind.values()) {
                if (kind.carries()) {
                    continue;
                }
                for (Tier tier : Tier.upTo(kind.top())) {
                    draw(output, writing, Skins.kindSkin(kind, tier), Skins.kind(kind, tier));
                }
            }
            // Two pictures for the things laid in lines, each drawn for the strip of
            // itself that a shape that thin actually shows.
            for (Tier tier : Tier.upTo(Kind.highestCarried())) {
                draw(output, writing, Skins.armSkin(tier), Skins.arm(tier));
                draw(output, writing, Skins.drillSkin(tier), Skins.drill(tier));
            }
            Path panel = screens.file(
                    ResourceLocation.fromNamespaceAndPath(Caldarium.MODID, Skins.GUI), "png");
            writing.add(CompletableFuture.runAsync(() -> write(output, Skins.gui(), panel),
                    Util.backgroundExecutor()));
            return CompletableFuture.allOf(writing.toArray(CompletableFuture[]::new));
        }

        private void draw(CachedOutput output, List<CompletableFuture<?>> writing, int[][] pixels,
                String name) {
            Path target = textures.file(
                    ResourceLocation.fromNamespaceAndPath(Caldarium.MODID, name), "png");
            writing.add(CompletableFuture.runAsync(() -> write(output, pixels, target),
                    Util.backgroundExecutor()));
        }

        @SuppressWarnings("deprecation") // Hashing.sha1 is what CachedOutput expects
        private static void write(CachedOutput output, int[][] pixels, Path target) {
            try {
                byte[] bytes = Png.encode(pixels);
                output.writeIfNeeded(target, bytes, Hashing.sha1().hashBytes(bytes));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public String getName() {
            return "Caldarium textures";
        }
    }

    private static class Models extends BlockStateProvider {
        /** Models shared between kinds or between tiers, so each is built once. */
        private final Set<String> drawn = new HashSet<>();

        Models(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Caldarium.MODID, existingFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            for (Generator.Made made : Generator.made()) {
                boolean flat = made.source().flat();
                String cold = flat ? made.id() : Skins.generator(made, false);
                String hot = flat ? made.id() + "_on" : Skins.generator(made, true);
                ModelFile unlit = flat ? panel(made, cold, false)
                        : models().cubeAll(cold, modLoc("block/" + cold));
                ModelFile lit = flat ? panel(made, hot, true)
                        : models().cubeAll(hot, modLoc("block/" + hot));
                getVariantBuilder(CaldariumRegistry.generators().get(made).get())
                        .forAllStates(state -> ConfiguredModel.builder()
                                .modelFile(state.getValue(GeneratorBlock.LIT) ? lit : unlit)
                                .build());
                // The item is the cold one: a generator in a hand is not burning.
                itemModels().withExistingParent(cold, modLoc("block/" + cold));
            }
            simpleBlockWithItem(CaldariumRegistry.SOL.get(),
                    models().cubeAll(Skins.SOL, modLoc("block/" + Skins.SOL)));
            for (Kind kind : Kind.values()) {
                for (Tier tier : Tier.upTo(kind.top())) {
                    String name = Skins.kind(kind, tier);
                    if (kind.carries()) {
                        carrier(kind, tier);
                        continue;
                    }
                    simpleBlock(CaldariumRegistry.block(kind, tier).get(),
                            models().cubeAll(name, modLoc("block/" + name)));
                    itemModels().withExistingParent(name, modLoc("block/" + name));
                }
            }
        }

        /**
         * A middle that is always drawn, and an arm for each side it is joined on.
         *
         * <p>Multipart rather than a variant for each of the sixty-four states: the
         * arms are the same six pictures however they are combined, and a variant list
         * would be the same model named sixty-four times over.
         */
        private void carrier(Kind kind, Tier tier) {
            var parts = getMultipartBuilder(CaldariumRegistry.block(kind, tier).get());
            parts.part().modelFile(core(kind, tier)).addModel().end();
            for (Direction side : Direction.values()) {
                EnumProperty<Joint> joint = CarrierBlock.JOINTS.get(side);
                if (!kind.door()) {
                    // A cable wears the same arm whichever side of the boundary it is
                    // joined to; only a door has two jobs to tell apart.
                    parts.part().modelFile(arm(tier, side)).addModel()
                            .condition(joint, Joint.ALONG).end();
                    continue;
                }
                parts.part().modelFile(arm(tier, side)).addModel()
                        .condition(joint, Joint.ALONG).end();
                parts.part().modelFile(drill(tier, side, kind.mouth())).addModel()
                        .condition(joint, Joint.AIMED).end();
            }
            held(kind, tier);
        }

        /**
         * What the block looks like in a hand.
         *
         * <p>A cable is joined on all six sides, because one in a hand is not pointing
         * anywhere yet and a bare middle would be a small grey box. A door gets one
         * drill and one arm instead, which is the shape of what it is for: a fitting
         * with a line on one side of it and something else on the other.
         */
        private void held(Kind kind, Tier tier) {
            var held = itemModels().getBuilder(Skins.kind(kind, tier))
                    .parent(models().getExistingFile(mcLoc("block/block")));
            ResourceLocation metal = modLoc("block/" + Skins.arm(tier));
            box(held, "middle", metal, Skins.middleBox(kind.core()));
            if (kind.door()) {
                box(held, "arm", metal, Skins.armBox(Direction.SOUTH), Direction.NORTH);
                ResourceLocation steps = modLoc("block/" + Skins.drill(tier));
                for (int step = 0; step < Skins.DRILL_STEPS; step++) {
                    box(held, "drill", steps,
                            Skins.drillBox(Direction.NORTH, step, kind.mouth()));
                }
                return;
            }
            for (Direction side : Direction.values()) {
                box(held, "arm", metal, Skins.armBox(side), side.getOpposite());
            }
        }

        /** The middle, wearing the face that says which of the three it is. */
        /**
         * The middle. ⭐ The same plain metal as the arms, so a run through a door has
         * neither a bulge nor a seam in it: at this thickness a middle shows two pixels
         * of picture, and two pixels cannot say anything a shape does not say better.
         */
        private ModelFile core(Kind kind, Tier tier) {
            String built = Skins.kind(kind, tier) + "_core";
            BlockModelBuilder model = models().getBuilder(built);
            if (drawn.add(built)) {
                model.parent(models().getExistingFile(mcLoc("block/block")));
                box(model, "middle", modLoc("block/" + Skins.arm(tier)),
                        Skins.middleBox(kind.core()));
            }
            return model;
        }

        /**
         * The steps a door puts on a face that reaches outside the line. The same six
         * of them serve both doors on a rung: what an importer and an exporter do at
         * such a face differs in direction, and direction is said on the middle.
         */
        private ModelFile drill(Tier tier, Direction side, boolean mouth) {
            String built = Skins.arm(tier) + (mouth ? "_mouth_" : "_nozzle_")
                    + side.getSerializedName();
            BlockModelBuilder model = models().getBuilder(built);
            if (drawn.add(built)) {
                model.parent(models().getExistingFile(mcLoc("block/block")));
                for (int step = 0; step < Skins.DRILL_STEPS; step++) {
                    box(model, "drill", modLoc("block/" + Skins.drill(tier)),
                            Skins.drillBox(side, step, mouth));
                }
            }
            return model;
        }

        /** One arm, in the metal of its rung. The same six for every kind on it. */
        private ModelFile arm(Tier tier, Direction side) {
            String built = Skins.arm(tier) + "_" + side.getSerializedName();
            BlockModelBuilder model = models().getBuilder(built);
            if (drawn.add(built)) {
                model.parent(models().getExistingFile(mcLoc("block/block")));
                box(model, "arm", modLoc("block/" + Skins.arm(tier)), Skins.armBox(side),
                        side.getOpposite());
            }
            return model;
        }

        /**
         * One box on a model.
         *
         * <p>⚠ No UV named anywhere, and that is deliberate. Left alone the game reads
         * the texture through the box, so a middle eight pixels across takes the eight
         * in the middle of the picture, which is exactly the window, and an arm takes a
         * strip of plain metal. Writing the UVs out by hand would be writing that same
         * arithmetic twice and getting it wrong the first time a size changed.
         */
        private static <T extends ModelBuilder<T>> void box(T model, String slot,
                ResourceLocation texture, int[] corners) {
            box(model, slot, texture, corners, null);
        }

        /**
         * ⚠ {@code leaveOff} is the face an arm presses against the middle. Drawn, it
         * would sit in the same plane as the middle's own face and the two would
         * flicker; it is also a face nobody can see, being inside the block.
         */
        private static <T extends ModelBuilder<T>> void box(T model, String slot,
                ResourceLocation texture, int[] corners, Direction leaveOff) {
            var element = model.texture("particle", texture).texture(slot, texture)
                    .element()
                    .from(corners[0], corners[1], corners[2])
                    .to(corners[3], corners[4], corners[5]);
            for (Direction face : Direction.values()) {
                if (face != leaveOff) {
                    element.face(face).texture("#" + slot).end();
                }
            }
            element.end();
        }

        /**
         * A plate as tall as {@link Skins#PANEL_HEIGHT}: the face it points at the
         * sky, and plain metal everywhere else. The sides take the top of the edge
         * texture, so what is seen is the top few pixels of a sheet of metal rather
         * than a squashed copy of the whole of it.
         */
        private ModelFile panel(Generator.Made made, String name, boolean lit) {
            String top = Skins.generatorTop(made, lit);
            String side = Skins.generatorSide(made);
            int tall = Skins.PANEL_HEIGHT;
            // ⚠ block/block, for its display transforms and nothing else. Without a
            // parent a model carries none, and the game drew this one square on to
            // its own edge: three pixels of panel, seen end on, in the inventory.
            return models().getBuilder(name)
                    .parent(models().getExistingFile(mcLoc("block/block")))
                    .texture("particle", modLoc("block/" + top))
                    .texture("top", modLoc("block/" + top))
                    .texture("side", modLoc("block/" + side))
                    .element()
                    .from(0, 0, 0)
                    .to(16, tall, 16)
                    .face(Direction.DOWN).texture("#side").cullface(Direction.DOWN).end()
                    .face(Direction.UP).texture("#top").end()
                    .face(Direction.NORTH).texture("#side").uvs(0, 0, 16, tall).end()
                    .face(Direction.SOUTH).texture("#side").uvs(0, 0, 16, tall).end()
                    .face(Direction.WEST).texture("#side").uvs(0, 0, 16, tall).end()
                    .face(Direction.EAST).texture("#side").uvs(0, 0, 16, tall).end()
                    .end()
                    // ⚠ A panel held in a hand sits where a whole block would, which
                    // for three pixels means down by the wrist and out of sight. It
                    // is lifted, and turned far enough to show the face rather than
                    // the edge - the face being the entire point of the block.
                    .transforms()
                    .transform(ItemDisplayContext.GUI)
                    .rotation(30, 225, 0).translation(0, 3, 0).scale(0.72F).end()
                    .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                    .rotation(25, 45, 0).translation(0, 6, 0).scale(0.5F).end()
                    .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                    .rotation(25, 225, 0).translation(0, 6, 0).scale(0.5F).end()
                    .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                    .rotation(75, 45, 0).translation(0, 5, 0).scale(0.42F).end()
                    .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                    .rotation(75, 45, 0).translation(0, 5, 0).scale(0.42F).end()
                    .end();
        }
    }

    private static class Language extends LanguageProvider {
        Language(PackOutput output) {
            super(output, Caldarium.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            for (Generator.Made made : Generator.made()) {
                add(CaldariumRegistry.generators().get(made).get(), titled(made.id()));
            }
            for (Kind kind : Kind.values()) {
                for (Tier tier : Tier.upTo(kind.top())) {
                    add(CaldariumRegistry.block(kind, tier).get(),
                            titled(tier.id()) + " " + titled(kind.getSerializedName()));
                }
            }
            add(CaldariumRegistry.SOL.get(), "Sol");
            add("itemGroup." + Caldarium.MODID, "Caldarium");
            add("gui.caldarium.stored", "%s / %s FE");
            add("gui.caldarium.held", "%s / %s mB");
            add("gui.caldarium.burning", "Burning: %s");
            add("gui.caldarium.pour.one", "1 Level");
            add("gui.caldarium.pour.ten", "10 Levels");
            add("gui.caldarium.pour.all", "All");
        }
    }

    /** Each block drops itself; there is nothing else it could reasonably do. */
    private static class Loot extends LootTableProvider {
        Loot(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, Set.of(),
                    List.of(new LootTableProvider.SubProviderEntry(Blocks::new,
                            LootContextParamSets.BLOCK)),
                    registries);
        }

        private static class Blocks extends BlockLootSubProvider {
            Blocks(HolderLookup.Provider registries) {
                super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
            }

            @Override
            protected void generate() {
                for (Block block : ours()) {
                    if (block instanceof SolBlock) {
                        // A sun that could be picked up would be a sun with a pause
                        // button, and its durability would only ever be spent by
                        // somebody who forgot to take it back.
                        add(block, noDrop());
                        continue;
                    }
                    dropSelf(block);
                }
            }

            @Override
            protected Iterable<Block> getKnownBlocks() {
                return ours();
            }
        }
    }

    private static List<Block> ours() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(CaldariumRegistry.SOL.get());
        CaldariumRegistry.generators().values().forEach(block -> blocks.add(block.get()));
        for (Kind kind : Kind.values()) {
            for (Tier tier : Tier.upTo(kind.top())) {
                blocks.add(CaldariumRegistry.block(kind, tier).get());
            }
        }
        return blocks;
    }

    /** An id, as a name. The ids are English words, so this is the whole of it. */
    private static String titled(String id) {
        StringBuilder name = new StringBuilder();
        for (String word : id.split("_")) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    /**
     * A pickaxe is what breaks these quickly. Only quickly - the blocks deliberately
     * do not require a correct tool, because failing to drop would mean losing what
     * was stored.
     */
    private static class Tags extends BlockTagsProvider {
        Tags(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                ExistingFileHelper existingFileHelper) {
            super(output, registries, Caldarium.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            tag(BlockTags.NEEDS_DIAMOND_TOOL).add(CaldariumRegistry.SOL.get());
            var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
            for (Block block : ours()) {
                pickaxe.add(block);
            }
        }
    }

    /**
     * Iron and redstone, and the vanilla block that already does the job in miniature:
     * a furnace for the thing that burns, a block of redstone for the thing that
     * stores. Deliberately cheap - how much either one does is a setting, so the
     * recipe is about when they become available rather than about how strong they are.
     */
    private static class Recipes extends RecipeProvider {
        /** How many a length of wire comes out as. Two ingots and a redstone. */
        private static final int CABLES_AT_ONCE = 6;

        Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(RecipeOutput output) {
            // A generator is its own metal and redstone around the vanilla thing that
            // already does its job by hand. A row with no rung of its own is iron, the
            // same rung its numbers are written for.
            for (Generator.Made made : Generator.made()) {
                Block block = CaldariumRegistry.generators().get(made).get();
                Tier under = made.tier() == null ? null : made.tier().under();
                if (under != null) {
                    Block below = CaldariumRegistry.generators()
                            .get(new Generator.Made(made.generator(), under)).get();
                    upgrade(output, block, below, made.tier(),
                            "has_" + new Generator.Made(made.generator(), under).id());
                    continue;
                }
                ItemLike own = made.tier() == null ? Items.IRON_INGOT : metal(made.tier());
                ShapedRecipeBuilder built = switch (made.source()) {
                    case ITEM -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("III")
                            .pattern("IFI")
                            .pattern("IRI")
                            .define('I', own)
                            .define('F', Blocks.FURNACE)
                            .define('R', Items.REDSTONE)
                            .unlockedBy("has_furnace", has(Blocks.FURNACE));
                    case FLUID -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("III")
                            .pattern("ICI")
                            .pattern("IRI")
                            .define('I', own)
                            .define('C', Blocks.CAULDRON)
                            .define('R', Items.REDSTONE)
                            .unlockedBy("has_cauldron", has(Blocks.CAULDRON));
                    case SUN -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("GGG")
                            .pattern("RRR")
                            .pattern("III")
                            .define('G', Blocks.GLASS)
                            .define('R', Items.REDSTONE)
                            .define('I', own)
                            .unlockedBy("has_glass", has(Blocks.GLASS));
                    case EXPERIENCE -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("CCC")
                            .pattern("CBC")
                            .pattern("CRC")
                            .define('C', own)
                            .define('B', Blocks.BOOKSHELF)
                            .define('R', Items.REDSTONE)
                            .unlockedBy("has_bookshelf", has(Blocks.BOOKSHELF));
                    case LIFE -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("CCC")
                            .pattern("CSC")
                            .pattern("CRC")
                            .define('C', own)
                            .define('S', Blocks.SOUL_SAND)
                            .define('R', Items.REDSTONE)
                            .unlockedBy("has_soul_sand", has(Blocks.SOUL_SAND));
                    case LAMP -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("GGG")
                            .pattern("RRR")
                            .pattern("CCC")
                            .define('G', Blocks.GLASS)
                            .define('R', Items.REDSTONE)
                            .define('C', own)
                            .unlockedBy("has_redstone", has(Items.REDSTONE));
                    // ⚠ A first pass and known to be one. What this must not be is the
                    // answer to "what do I power the early game with" - a strike is
                    // worth more than anything else here makes in a minute, so the way
                    // in has to sit well past the early game. A copper rod does not.
                    case STORM -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("NLN")
                            .pattern("LDL")
                            .pattern("NLN")
                            .define('N', Items.NETHERITE_INGOT)
                            .define('L', Blocks.LIGHTNING_ROD)
                            .define('D', Blocks.DIAMOND_BLOCK)
                            .unlockedBy("has_lightning_rod", has(Blocks.LIGHTNING_ROD));
                    case BLOW -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("CLC")
                            .pattern("CLC")
                            .pattern("CRC")
                            .define('C', own)
                            .define('L', Blocks.OAK_LOG)
                            .define('R', Items.REDSTONE)
                            .unlockedBy("has_redstone", has(Items.REDSTONE));
                    case HEAT -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("IBI")
                            .pattern("BRB")
                            .pattern("IBI")
                            .define('I', own)
                            .define('B', Blocks.BRICKS)
                            .define('R', Items.REDSTONE)
                            .unlockedBy("has_bricks", has(Blocks.BRICKS));
                };
                built.save(output);
            }

            // The first rung is built from parts. Every rung above it is the rung
            // below in a frame of its own metal, so a tier added to the table brings
            // its recipe with it and the ladder cannot grow a missing step.
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, CaldariumRegistry.SOL.get())
                    .pattern("FNF")
                    .pattern("NSN")
                    .pattern("FNF")
                    .define('F', Items.FIRE_CHARGE)
                    .define('N', Items.NETHERITE_INGOT)
                    .define('S', Items.NETHER_STAR)
                    .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                    .save(output);

            for (Kind kind : Kind.values()) {
                for (Tier tier : Tier.upTo(kind.top())) {
                    recipe(output, kind, tier);
                }
            }
        }

        /**
         * How one block of one kind at one tier is made.
         *
         * <p>⭐ Exhaustive, so a kind added to the table stops the build here rather
         * than quietly coming out of the crafting table wearing the battery's recipe.
         */
        private void recipe(RecipeOutput output, Kind kind, Tier tier) {
            switch (kind) {
                case BATTERY -> laddered(output, kind, tier, "IRI", "RBR", "IRI");
                // A charger is a battery you can reach into, so its frame is open at
                // the top.
                case CHARGER -> laddered(output, kind, tier, "I I", "RBR", "IRI");
                // The two that cross the boundary are the vanilla blocks that already
                // do it by hand, in a frame: a hopper takes out of something, and a
                // dropper puts into it.
                case IMPORTER -> boundary(output, kind, tier, Blocks.HOPPER, "has_hopper");
                case EXPORTER -> boundary(output, kind, tier, Blocks.DROPPER, "has_dropper");
                case CABLE -> wire(output, tier);
            }
        }

        /**
         * A rung of a ladder: redstone around the vanilla block that already does the
         * job in miniature on the first one, and the rung below in a frame of its own
         * metal on every one after.
         */
        private void laddered(RecipeOutput output, Kind kind, Tier tier, String... rows) {
            if (tier.under() != null) {
                upgrade(output, kind, tier);
                return;
            }
            ShapedRecipeBuilder built = ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE,
                    CaldariumRegistry.block(kind, tier).get());
            for (String row : rows) {
                built = built.pattern(row);
            }
            built.define('I', metal(tier))
                    .define('R', Items.REDSTONE)
                    .define('B', Blocks.REDSTONE_BLOCK)
                    .unlockedBy("has_redstone_block", has(Blocks.REDSTONE_BLOCK))
                    .save(output);
        }

        /** The way in or the way out: a vanilla block that already does it, framed. */
        private void boundary(RecipeOutput output, Kind kind, Tier tier, ItemLike core,
                String unlocked) {
            if (tier.under() != null) {
                upgrade(output, kind, tier);
                return;
            }
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE,
                            CaldariumRegistry.block(kind, tier).get())
                    .pattern(" M ")
                    .pattern("MCM")
                    .pattern(" M ")
                    .define('M', metal(tier))
                    .define('C', core)
                    .unlockedBy(unlocked, has(core))
                    .save(output);
        }

        /**
         * ⚠ <b>The one thing here that is not a ladder.</b> Every other kind is built
         * from the rung below, which costs four ingots a block — and a cable is not a
         * machine you improve but wire you draw, wanted by the hundred and laid in
         * lines a hundred long. Four netherite ingots each would be a tier nobody
         * could ever use. It is made from its own metal at every rung instead, and
         * several at a time.
         */
        private void wire(RecipeOutput output, Tier tier) {
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE,
                            CaldariumRegistry.block(Kind.CABLE, tier).get(), CABLES_AT_ONCE)
                    .pattern("MRM")
                    .define('M', metal(tier))
                    .define('R', Items.REDSTONE)
                    .unlockedBy("has_redstone", has(Items.REDSTONE))
                    .save(output);
        }

        private void upgrade(RecipeOutput output, Kind kind, Tier tier) {
            upgrade(output, CaldariumRegistry.block(kind, tier).get(),
                    CaldariumRegistry.block(kind, tier.under()).get(), tier,
                    "has_" + kind.id(tier.under()));
        }

        private void upgrade(RecipeOutput output, ItemLike result, ItemLike under, Tier tier,
                String unlocked) {
            ShapedRecipeBuilder built = tier.compressed()
                    ? ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, result)
                            .pattern("PPP")
                            .pattern("PMP")
                            .pattern("PPP")
                            .define('P', under)
                            .define('M', medium(tier))
                    : ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, result)
                            .pattern(" M ")
                            .pattern("MPM")
                            .pattern(" M ")
                            .define('M', metal(tier))
                            .define('P', under);
            built.unlockedBy(unlocked, has(under)).save(output);
        }

        /**
         * What squeezes eight of the rung below into one. Eight blocks and one of these.
         *
         * <p>Neither is picked for being dear. Sixty-four of the sixth rung go into one of
         * the eighth and each of those took four nether stars, so the withers are the
         * price and a tear is a rounding error against them. What these choose is which
         * places you have to have been.
         *
         * <p>A tear rather than anything newer because it has been in the game since the
         * beta, and this mod may be carried back to older versions.
         */
        private static ItemLike medium(Tier tier) {
            return switch (tier) {
                case COMPRESSED_NETHER_STAR -> Items.GHAST_TEAR;
                case SUPER_COMPRESSED_NETHER_STAR -> Items.TOTEM_OF_UNDYING;
                case COPPER, IRON, GOLD, DIAMOND, NETHERITE, NETHER_STAR ->
                        throw new IllegalStateException(tier.id()
                                + " is built from a frame, so it squeezes nothing");
            };
        }

        /** What a rung is made of. The ladder is the vanilla one everybody knows. */
        private static ItemLike metal(Tier tier) {
            return switch (tier) {
                case COPPER -> Items.COPPER_INGOT;
                case IRON -> Items.IRON_INGOT;
                case GOLD -> Items.GOLD_INGOT;
                case DIAMOND -> Items.DIAMOND;
                case NETHERITE -> Items.NETHERITE_INGOT;
                case NETHER_STAR -> Items.NETHER_STAR;
                case COMPRESSED_NETHER_STAR, SUPER_COMPRESSED_NETHER_STAR ->
                        throw new IllegalStateException(tier.id()
                                + " is nine of the rung below, so it asks for no metal");
            };
        }
    }
}
