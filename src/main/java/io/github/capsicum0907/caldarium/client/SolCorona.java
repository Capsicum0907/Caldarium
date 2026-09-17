package io.github.capsicum0907.caldarium.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import io.github.capsicum0907.caldarium.SolPalette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class SolCorona {
    private static final int SEGMENTS = 96;
    private static final double RIM_ANGLE = 0.0025;
    private static final float CORONA = 1.3F;
    private static final float BLAZING_CORONA = 1.6F;
    private static final float GLOW_ALPHA = 0.55F;
    private static final float BLAZING_GLOW_ALPHA = 0.8F;
    private static final int BUFFER = 1536;

    private static final RenderType GLOW = RenderType.create("caldarium_corona",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, BUFFER, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                    .createCompositeState(false));

    private static final List<Pending> PENDING = new ArrayList<>();

    private record Pending(Vec3 centre, float radius, float blaze) {
    }

    public record Limb(Vec3 plane, Vec3 across, Vec3 upward, double edge, double inner, double outer) {
        public static Limb of(Vec3 toCamera, float radius) {
            double distance = toCamera.length();
            Vec3 towards = toCamera.scale(1.0 / distance);
            Vec3 facing = towards.reverse();
            Vec3 up = Math.abs(towards.y) > 0.99 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
            Vec3 across = facing.cross(up).normalize();
            Vec3 upward = across.cross(facing);
            Vec3 plane = towards.scale(radius * radius / distance);
            double edge = radius * Math.sqrt(1.0 - (radius / distance) * (radius / distance));
            double line = RIM_ANGLE * Math.sqrt(distance * distance - radius * radius);
            return new Limb(plane, across, upward, edge, edge - line, edge + line);
        }

        public Vec3 at(float angle, double reach) {
            return plane.add(across.scale(Mth.cos(angle) * reach)).add(upward.scale(Mth.sin(angle) * reach));
        }
    }

    private SolCorona() {
    }

    public static float reach() {
        return BLAZING_CORONA;
    }

    public static int segments() {
        return SEGMENTS;
    }

    public static void queue(Vec3 centre, float radius, float blaze) {
        PENDING.add(new Pending(centre, radius, blaze));
    }

    public static void draw(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || PENDING.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PENDING.sort(Comparator.comparingDouble((Pending pending) -> pending.centre().distanceToSqr(camera))
                .reversed());
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer into = buffers.getBuffer(GLOW);
        PoseStack.Pose pose = event.getPoseStack().last();
        for (Pending pending : PENDING) {
            Vec3 offset = pending.centre().subtract(camera);
            Limb limb = Limb.of(offset.reverse(), pending.radius());
            float corona = Mth.lerp(pending.blaze(), CORONA, BLAZING_CORONA);
            float alpha = Mth.lerp(pending.blaze(), GLOW_ALPHA, BLAZING_GLOW_ALPHA);
            int colour = SolPalette.glow(pending.blaze());
            for (int i = 0; i < SEGMENTS; i++) {
                float from = Mth.TWO_PI * i / SEGMENTS;
                float to = Mth.TWO_PI * (i + 1) / SEGMENTS;
                shine(pose, into, offset.add(limb.at(from, limb.outer())), colour, alpha);
                shine(pose, into, offset.add(limb.at(from, limb.edge() * corona)), colour, 0.0F);
                shine(pose, into, offset.add(limb.at(to, limb.edge() * corona)), colour, 0.0F);
                shine(pose, into, offset.add(limb.at(to, limb.outer())), colour, alpha);
            }
        }
        PENDING.clear();
        buffers.endBatch(GLOW);
    }

    private static void shine(PoseStack.Pose pose, VertexConsumer into, Vec3 point, int colour, float alpha) {
        into.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(SolPalette.channel(colour, 16), SolPalette.channel(colour, 8),
                        SolPalette.channel(colour, 0), alpha);
    }
}
