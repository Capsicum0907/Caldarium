package io.github.capsicum0907.caldarium;

import com.mojang.logging.LogUtils;

import io.github.capsicum0907.caldarium.client.CaldariumClient;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
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
        CaldariumRegistry.TABS.register(modEventBus);

        modEventBus.addListener(Caldarium::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(Caldarium::lightning);
        NeoForge.EVENT_BUS.addListener(Caldarium::placing);
        NeoForge.EVENT_BUS.addListener(Caldarium::aiming);
        NeoForge.EVENT_BUS.addListener(Caldarium::spoils);
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
    private static void spoils(LivingDropsEvent event) {
        if (event.getSource().is(CaldariumRegistry.SPOLIARIUM_DAMAGE)) {
            event.setCanceled(true);
        }
    }

    private static void lightning(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.LightningBolt bolt) {
            Storm.struck(bolt);
        }
    }

    public static void aiming(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getItemStack().getItem() instanceof BlockItem)) {
            return;
        }
        BlockPos pos = new BlockPlaceContext(new UseOnContext(event.getEntity(), event.getHand(),
                event.getHitVec())).getClickedPos();
        if (Suns.inside(event.getLevel(), pos)) {
            event.setUseItem(TriState.FALSE);
        }
    }

    private static void placing(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof net.minecraft.world.level.Level level
                && Suns.inside(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                CaldariumRegistry.GENERATOR_ENTITY.get(), (generator, side) -> generator.store());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                CaldariumRegistry.GENERATOR_ENTITY.get(), (generator, side) -> generator.fuel());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                CaldariumRegistry.GENERATOR_ENTITY.get(), (generator, side) -> generator.tank());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                CaldariumRegistry.KIND_ENTITY.get(),
                (machine, side) -> machine.reachable(side) ? machine.store() : null);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                CaldariumRegistry.KIND_ENTITY.get(), (machine, side) -> machine.items());
    }

    /** Drawing is a client concern, and this is the only place that knows it exists. */
    @Mod(value = MODID, dist = Dist.CLIENT)
    public static class Client {
        public Client(IEventBus modEventBus, ModContainer modContainer) {
            modEventBus.addListener(CaldariumClient::registerScreens);
            modEventBus.addListener(CaldariumClient::registerRenderers);
            modEventBus.addListener(CaldariumClient::registerExtensions);
            modEventBus.addListener(CaldariumClient::registerTooltips);
            NeoForge.EVENT_BUS.addListener(CaldariumClient::readings);
            NeoForge.EVENT_BUS.addListener(CaldariumClient::outline);
            NeoForge.EVENT_BUS.addListener(CaldariumClient::preview);
            NeoForge.EVENT_BUS.addListener(io.github.capsicum0907.caldarium.client.SolCorona::draw);
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
