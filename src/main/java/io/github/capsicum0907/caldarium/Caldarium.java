package io.github.capsicum0907.caldarium;

import com.mojang.logging.LogUtils;

import io.github.capsicum0907.caldarium.client.CaldariumClient;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;

import org.slf4j.Logger;

/**
 * Entry point. {@link #MODID} must match {@code mod_id} in gradle.properties,
 * which is what the generated neoforge.mods.toml is filled from.
 */
@Mod(Caldarium.MODID)
public class Caldarium {
    public static final String MODID = "caldarium";

    public static final Logger LOGGER = LogUtils.getLogger();

    public Caldarium(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, CaldariumConfig.SPEC);

        CaldariumRegistry.BLOCKS.register(modEventBus);
        CaldariumRegistry.ITEMS.register(modEventBus);
        CaldariumRegistry.BLOCK_ENTITIES.register(modEventBus);
        CaldariumRegistry.MENUS.register(modEventBus);

        modEventBus.addListener(Caldarium::registerCapabilities);
        modEventBus.addListener(Caldarium::addToCreativeTab);

        LOGGER.info("Caldarium {} loaded.", modContainer.getModInfo().getVersion());
    }

    /**
     * The whole of the integration. Everything that moves energy — this mod's own
     * push, another mod's cable, a machine drawing from what it is stood on — asks a
     * block for the energy capability and for nothing else.
     *
     * <p>Registered without regard to side: a face that behaved differently would be
     * a routing decision, and this mod does not make those. The generator also offers
     * its fuel slot as an item handler, which is how a hopper feeds it.
     */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                CaldariumRegistry.GENERATOR_ENTITY.get(), (generator, side) -> generator.store());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                CaldariumRegistry.GENERATOR_ENTITY.get(), (generator, side) -> generator.fuel());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                CaldariumRegistry.GENERATOR_ENTITY.get(), (generator, side) -> generator.tank());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                CaldariumRegistry.KIND_ENTITY.get(), (machine, side) -> machine.store());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                CaldariumRegistry.KIND_ENTITY.get(), (machine, side) -> machine.items());
    }

    /** Drawing is a client concern, and this is the only place that knows it exists. */
    @Mod(value = MODID, dist = Dist.CLIENT)
    public static class Client {
        public Client(IEventBus modEventBus, ModContainer modContainer) {
            modEventBus.addListener(CaldariumClient::registerScreens);
        }
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            for (DeferredItem<?> item : CaldariumRegistry.items()) {
                event.accept(item);
            }
        }
    }
}
