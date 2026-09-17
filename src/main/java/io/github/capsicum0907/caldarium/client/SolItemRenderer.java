package io.github.capsicum0907.caldarium.client;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.capsicum0907.caldarium.SolPalette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SolItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float ICON_RADIUS = 0.5F;

    public SolItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();
        float time = (minecraft.level == null ? 0L : minecraft.level.getGameTime())
                + minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        SolRenderer.draw(pose, buffers, overlay, ICON_RADIUS, time, SolPalette.blaze(time, 0L), null);
    }
}
