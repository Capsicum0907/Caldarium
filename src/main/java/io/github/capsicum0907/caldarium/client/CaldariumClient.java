package io.github.capsicum0907.caldarium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.capsicum0907.caldarium.CaldariumRegistry;
import io.github.capsicum0907.caldarium.SolBlock;
import io.github.capsicum0907.caldarium.SolBlockEntity;
import io.github.capsicum0907.caldarium.SolItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Drawing is a client concern, and this is the only place that knows it exists. */
public final class CaldariumClient {
    private static final int OUTLINE_SEGMENTS = 64;
    private static final float OUTLINE_ALPHA = 0.4F;
    private static final float OUTLINE_LIFT = 0.002F;
    private static final int OUTLINE = 0x000000;
    private static final int FITS = 0xFFFFFF;
    private static final int BLOCKED = 0xFF3030;
    private static final float PREVIEW_ALPHA = 0.8F;

    private CaldariumClient() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CaldariumRegistry.MACHINE_MENU.get(), MachineScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CaldariumRegistry.SOL_ENTITY.get(), SolRenderer::new);
        SolBlockEntity.onLeavingClient(SolRenderer::forget);
    }

    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private SolItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new SolItemRenderer();
                }
                return renderer;
            }
        }, CaldariumRegistry.SOL_ITEM.get());
    }

    public static void outline(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        BlockPos pos = event.getTarget().getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        if (state.getBlock() instanceof SolBlock) {
            event.setCanceled(true);
            wire(event, pos, SolBlock.radius(state) + OUTLINE_LIFT, OUTLINE, OUTLINE_ALPHA);
            return;
        }
    }

    public static void preview(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES
                || player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).getItem() instanceof SolItem item) {
                BlockState placed = item.getBlock().defaultBlockState();
                BlockPos core = SolItem.core(player, placed);
                boolean fits = SolItem.fits(minecraft.level, player, core, placed);
                MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
                wire(event.getPoseStack(), buffers, event.getCamera().getPosition(), core,
                        SolBlock.radius(placed), fits ? FITS : BLOCKED, PREVIEW_ALPHA);
                buffers.endBatch(RenderType.lines());
                return;
            }
        }
    }

    private static void wire(RenderHighlightEvent.Block event, BlockPos pos, float radius, int colour,
            float alpha) {
        wire(event.getPoseStack(), event.getMultiBufferSource(), event.getCamera().getPosition(), pos,
                radius, colour, alpha);
    }

    private static void wire(PoseStack pose, MultiBufferSource buffers, Vec3 camera, BlockPos pos,
            float radius, int colour, float alpha) {
        Vec3 centre = SolBlock.centre(pos).subtract(camera);
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        for (int axis = 0; axis < 3; axis++) {
            for (int i = 0; i < OUTLINE_SEGMENTS; i++) {
                float from = Mth.TWO_PI * i / OUTLINE_SEGMENTS;
                float to = Mth.TWO_PI * (i + 1) / OUTLINE_SEGMENTS;
                point(pose, lines, centre, radius, axis, from, to, colour, alpha);
                point(pose, lines, centre, radius, axis, to, from, colour, alpha);
            }
        }
    }

    private static void point(PoseStack pose, VertexConsumer lines, Vec3 centre, float radius,
            int axis, float at, float away, int colour, float alpha) {
        Vec3 here = circle(axis, at);
        Vec3 along = circle(axis, away).subtract(here).normalize();
        lines.addVertex(pose.last(), (float) (centre.x + here.x * radius),
                        (float) (centre.y + here.y * radius), (float) (centre.z + here.z * radius))
                .setColor(((colour >> 16) & 0xFF) / 255.0F, ((colour >> 8) & 0xFF) / 255.0F,
                        (colour & 0xFF) / 255.0F, alpha)
                .setNormal(pose.last(), (float) along.x, (float) along.y, (float) along.z);
    }

    private static Vec3 circle(int axis, float angle) {
        float a = Mth.cos(angle);
        float b = Mth.sin(angle);
        return switch (axis) {
            case 0 -> new Vec3(0.0, a, b);
            case 1 -> new Vec3(a, 0.0, b);
            default -> new Vec3(a, b, 0.0);
        };
    }
}
