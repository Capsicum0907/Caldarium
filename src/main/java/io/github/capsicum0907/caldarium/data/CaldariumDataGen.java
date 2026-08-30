package io.github.capsicum0907.caldarium.data;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.common.hash.Hashing;

import io.github.capsicum0907.caldarium.Caldarium;
import io.github.capsicum0907.caldarium.CaldariumRegistry;
import io.github.capsicum0907.caldarium.Generator;
import io.github.capsicum0907.caldarium.Kind;
import io.github.capsicum0907.caldarium.GeneratorBlock;
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
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
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
                for (Tier tier : Tier.values()) {
                    draw(output, writing, Skins.kindSkin(kind, tier), Skins.kind(kind, tier));
                }
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
            for (Kind kind : Kind.values()) {
                for (Tier tier : Tier.values()) {
                    String name = Skins.kind(kind, tier);
                    simpleBlock(CaldariumRegistry.block(kind, tier).get(),
                            models().cubeAll(name, modLoc("block/" + name)));
                    itemModels().withExistingParent(name, modLoc("block/" + name));
                }
            }
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
                for (Tier tier : Tier.values()) {
                    add(CaldariumRegistry.block(kind, tier).get(),
                            titled(tier.id()) + " " + titled(kind.getSerializedName()));
                }
            }
            add("itemGroup." + Caldarium.MODID, "Caldarium");
            add("gui.caldarium.stored", "%s / %s FE");
            add("gui.caldarium.held", "%s / %s mB");
            add("gui.caldarium.burning", "Burning: %s");
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
        CaldariumRegistry.generators().values().forEach(block -> blocks.add(block.get()));
        for (Kind kind : Kind.values()) {
            for (Tier tier : Tier.values()) {
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
            // Each generator is iron and redstone around the vanilla thing that
            // already does its job by hand: a furnace to burn, a cauldron to hold
            // something molten, and glass to face the sky. A generator that stands on
            // a ladder is built from parts on its first rung and from the rung below
            // on every one after, the same as everything else that has tiers.
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
                switch (made.source()) {
                    case ITEM -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("III")
                            .pattern("IFI")
                            .pattern("IRI")
                            .define('I', Items.IRON_INGOT)
                            .define('F', Blocks.FURNACE)
                            .define('R', Items.REDSTONE)
                            .unlockedBy("has_furnace", has(Blocks.FURNACE))
                            .save(output);
                    case FLUID -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("III")
                            .pattern("ICI")
                            .pattern("IRI")
                            .define('I', Items.IRON_INGOT)
                            .define('C', Blocks.CAULDRON)
                            .define('R', Items.REDSTONE)
                            .unlockedBy("has_cauldron", has(Blocks.CAULDRON))
                            .save(output);
                    case SUN -> ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, block)
                            .pattern("GGG")
                            .pattern("RRR")
                            .pattern("III")
                            .define('G', Blocks.GLASS)
                            .define('R', Items.REDSTONE)
                            .define('I', Items.IRON_INGOT)
                            .unlockedBy("has_glass", has(Blocks.GLASS))
                            .save(output);
                }
            }

            // The first rung is built from parts. Every rung above it is the rung
            // below in a frame of its own metal, so a tier added to the table brings
            // its recipe with it and the ladder cannot grow a missing step.
            for (Kind kind : Kind.values()) {
                for (Tier tier : Tier.values()) {
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

        /** One rung: the thing below it, in a frame of the metal this rung is made of. */
        private void upgrade(RecipeOutput output, ItemLike result, ItemLike under, Tier tier,
                String unlocked) {
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, result)
                    .pattern(" M ")
                    .pattern("MPM")
                    .pattern(" M ")
                    .define('M', metal(tier))
                    .define('P', under)
                    .unlockedBy(unlocked, has(under))
                    .save(output);
        }

        /** What a rung is made of. The ladder is the vanilla one everybody knows. */
        private static ItemLike metal(Tier tier) {
            return switch (tier) {
                case IRON -> Items.IRON_INGOT;
                case GOLD -> Items.GOLD_INGOT;
                case DIAMOND -> Items.DIAMOND;
                case NETHERITE -> Items.NETHERITE_INGOT;
            };
        }
    }
}
