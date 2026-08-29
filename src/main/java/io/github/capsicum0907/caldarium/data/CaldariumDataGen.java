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
import io.github.capsicum0907.caldarium.GeneratorBlock;
import io.github.capsicum0907.caldarium.Tier;

import net.minecraft.Util;
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
import net.minecraft.world.item.Items;
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
            for (Generator generator : Generator.all()) {
                for (boolean lit : new boolean[] { false, true }) {
                    draw(output, writing, Skins.generatorSkin(lit), Skins.generator(generator, lit));
                }
            }
            for (Tier tier : Tier.values()) {
                draw(output, writing, Skins.batterySkin(tier), Skins.battery(tier));
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
            for (Generator generator : Generator.all()) {
                String cold = Skins.generator(generator, false);
                String hot = Skins.generator(generator, true);
                ModelFile unlit = models().cubeAll(cold, modLoc("block/" + cold));
                ModelFile lit = models().cubeAll(hot, modLoc("block/" + hot));
                getVariantBuilder(CaldariumRegistry.generators().get(generator).get())
                        .forAllStates(state -> ConfiguredModel.builder()
                                .modelFile(state.getValue(GeneratorBlock.LIT) ? lit : unlit)
                                .build());
                // The item is the cold one: a generator in a hand is not burning.
                itemModels().withExistingParent(cold, modLoc("block/" + cold));
            }
            for (Tier tier : Tier.values()) {
                String name = Skins.battery(tier);
                simpleBlock(CaldariumRegistry.batteries().get(tier).get(),
                        models().cubeAll(name, modLoc("block/" + name)));
                itemModels().withExistingParent(name, modLoc("block/" + name));
            }
        }
    }

    private static class Language extends LanguageProvider {
        Language(PackOutput output) {
            super(output, Caldarium.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add(CaldariumRegistry.generators().get(Generator.BURNER).get(), "Burner");
            add(CaldariumRegistry.batteries().get(Tier.IRON).get(), "Iron Battery");
            add("gui.caldarium.stored", "%s / %s FE");
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
        CaldariumRegistry.batteries().values().forEach(block -> blocks.add(block.get()));
        return blocks;
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
        Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(RecipeOutput output) {
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE,
                            CaldariumRegistry.generators().get(Generator.BURNER).get())
                    .pattern("III")
                    .pattern("IFI")
                    .pattern("IRI")
                    .define('I', Items.IRON_INGOT)
                    .define('F', Blocks.FURNACE)
                    .define('R', Items.REDSTONE)
                    .unlockedBy("has_furnace", has(Blocks.FURNACE))
                    .save(output);

            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE,
                            CaldariumRegistry.batteries().get(Tier.IRON).get())
                    .pattern("IRI")
                    .pattern("RBR")
                    .pattern("IRI")
                    .define('I', Items.IRON_INGOT)
                    .define('R', Items.REDSTONE)
                    .define('B', Blocks.REDSTONE_BLOCK)
                    .unlockedBy("has_redstone_block", has(Blocks.REDSTONE_BLOCK))
                    .save(output);
        }
    }
}
