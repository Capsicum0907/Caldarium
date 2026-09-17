package io.github.capsicum0907.caldarium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.capsicum0907.caldarium.CaldariumRegistry;
import io.github.capsicum0907.caldarium.SolBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

/** Drawing is a client concern, and this is the only place that knows it exists. */
public final class CaldariumClient {
    private static final int OUTLINE_SEGMENTS = 64;
    private static final float OUTLINE_ALPHA = 0.4F;
    private static final float OUTLINE_LIFT = 0.002F;

    private CaldariumClient() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CaldariumRegistry.MACHINE_MENU.get(), MachineScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CaldariumRegistry.SOL_ENTITY.get(), SolRenderer::new);
    }

    public static void outline(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        BlockPos pos = event.getTarget().getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        if (!(state.getBlock() instanceof SolBlock)) {
            return;
        }
        event.setCanceled(true);

        Vec3 centre = SolBlock.centre(pos).subtract(event.getCamera().getPosition());
        float radius = SolBlock.radius(state) + OUTLINE_LIFT;
        PoseStack pose = event.getPoseStack();
        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        for (int axis = 0; axis < 3; axis++) {
            for (int i = 0; i < OUTLINE_SEGMENTS; i++) {
                float from = Mth.TWO_PI * i / OUTLINE_SEGMENTS;
                float to = Mth.TWO_PI * (i + 1) / OUTLINE_SEGMENTS;
                point(pose, lines, centre, radius, axis, from, to);
                point(pose, lines, centre, radius, axis, to, from);
            }
        }
    }

    private static void point(PoseStack pose, VertexConsumer lines, Vec3 centre, float radius,
            int axis, float at, float away) {
        Vec3 here = circle(axis, at);
        Vec3 along = circle(axis, away).subtract(here).normalize();
        lines.addVertex(pose.last(), (float) (centre.x + here.x * radius),
                        (float) (centre.y + here.y * radius), (float) (centre.z + here.z * radius))
                .setColor(0.0F, 0.0F, 0.0F, OUTLINE_ALPHA)
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
