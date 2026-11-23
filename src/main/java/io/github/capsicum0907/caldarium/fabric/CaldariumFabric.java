package io.github.capsicum0907.caldarium.fabric;

import java.nio.file.Path;

import io.github.capsicum0907.caldarium.Caldarium;
import io.github.capsicum0907.caldarium.CaldariumConfig;
import io.github.capsicum0907.caldarium.CaldariumRegistry;
import io.github.capsicum0907.caldarium.SolBlockEntity;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.storage.LevelResource;
//? if >=1.20 {
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
//?}

import team.reborn.energy.api.EnergyStorage;

public final class CaldariumFabric implements ModInitializer {
    private static final String CONFIG_FOLDER = "serverconfig";
    private static final String CONFIG_FILE = Caldarium.MODID + "-server.toml";

    public static Path config(Path world) {
        return world.resolve(CONFIG_FOLDER).resolve(CONFIG_FILE);
    }

    @Override
    public void onInitialize() {
        CaldariumRegistry.BLOCKS.register();
        CaldariumRegistry.ITEMS.register();
        CaldariumRegistry.BLOCK_ENTITIES.register();
        CaldariumRegistry.MENUS.register();
        //? if >=1.19.3
        CaldariumRegistry.TABS.register();

        EnergyStorage.SIDED.registerForBlockEntity(
                (generator, side) -> Energies.of(generator.store()), CaldariumRegistry.GENERATOR_ENTITY.get());
        EnergyStorage.SIDED.registerForBlockEntity(
                (machine, side) -> machine.reachable(side) ? Energies.of(machine.store()) : null,
                CaldariumRegistry.KIND_ENTITY.get());
        ItemStorage.SIDED.registerForBlockEntity(
                (generator, side) -> Transfer.items(generator.fuel()), CaldariumRegistry.GENERATOR_ENTITY.get());
        ItemStorage.SIDED.registerForBlockEntity(
                (machine, side) -> Transfer.items(machine.items()), CaldariumRegistry.KIND_ENTITY.get());
        FluidStorage.SIDED.registerForBlockEntity(
                (generator, side) -> Transfer.fluids(generator.tank()), CaldariumRegistry.GENERATOR_ENTITY.get());

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof SolBlockEntity sol) {
                sol.onLoad();
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof SolBlockEntity sol) {
                sol.onChunkUnloaded();
            }
        });

        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                Caldarium.intoSun(level, player, hand, player.getItemInHand(hand), hit)
                        ? InteractionResult.FAIL : InteractionResult.PASS);

        //? if >=1.20 {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries ->
                CaldariumRegistry.items().forEach(item -> entries.accept(item.get())));
        //?}

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                CaldariumConfig.SPEC.load(config(server.getWorldPath(LevelResource.ROOT))));

        Caldarium.LOGGER.info("Caldarium {} loaded.", FabricLoader.getInstance().getModContainer(Caldarium.MODID)
                .orElseThrow(IllegalStateException::new).getMetadata().getVersion().getFriendlyString());
    }
}
