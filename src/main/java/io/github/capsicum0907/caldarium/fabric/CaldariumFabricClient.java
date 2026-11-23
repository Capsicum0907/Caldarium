package io.github.capsicum0907.caldarium.fabric;

import io.github.capsicum0907.caldarium.CaldariumRegistry;
import io.github.capsicum0907.caldarium.SolBlockEntity;
import io.github.capsicum0907.caldarium.Tooltips;
import io.github.capsicum0907.caldarium.client.CaldariumClient;
import io.github.capsicum0907.caldarium.client.MachineScreen;
import io.github.capsicum0907.caldarium.client.ReadingsTooltip;
import io.github.capsicum0907.caldarium.client.SolCorona;
import io.github.capsicum0907.caldarium.client.SolItemRenderer;
import io.github.capsicum0907.caldarium.client.SolRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class CaldariumFabricClient implements ClientModInitializer {
    private SolItemRenderer solItem;

    @Override
    public void onInitializeClient() {
        MenuScreens.register(CaldariumRegistry.MACHINE_MENU.get(), MachineScreen::new);
        BlockEntityRendererRegistry.register(CaldariumRegistry.SOL_ENTITY.get(), SolRenderer::new);
        SolBlockEntity.onLeavingClient(SolRenderer::forget);
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof SolBlockEntity sol) {
                sol.onLoad();
            }
        });
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof SolBlockEntity sol) {
                sol.onChunkUnloaded();
            }
        });
        BuiltinItemRendererRegistry.INSTANCE.register(CaldariumRegistry.SOL_ITEM.get(),
                (stack, context, pose, buffers, light, overlay) -> {
                    if (solItem == null) {
                        solItem = new SolItemRenderer();
                    }
                    solItem.renderByItem(stack, context, pose, buffers, light, overlay);
                });
        TooltipComponentCallback.EVENT.register(data ->
                data instanceof Tooltips.Readings readings ? new ReadingsTooltip(readings) : null);

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hit) ->
                !(hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK
                        && CaldariumClient.drawOutline(context.matrixStack(), context.consumers(),
                                context.camera().getPosition(), block.getBlockPos())));
        WorldRenderEvents.AFTER_ENTITIES.register(context ->
                CaldariumClient.drawPreview(context.matrixStack(), context.camera().getPosition()));
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context ->
                SolCorona.drawQueued(context.matrixStack(), context.camera().getPosition()));
    }
}
