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
    private static final int RINGS = 8;
    private static final double RIM_ANGLE = 0.0025;
    private static final float HALO_REACH = 1.16F;
    private static final float BLAZING_HALO_REACH = 1.32F;
    private static final float HALO_ALPHA = 0.75F;
    private static final float BLAZING_HALO_ALPHA = 0.95F;
    private static final float HALO_FALLOFF = 2.6F;
    private static final float RAY_REACH = 2.0F;
    private static final float BLAZING_RAY_REACH = 2.4F;
    private static final float RAY_ALPHA = 0.42F;
    private static final float BLAZING_RAY_ALPHA = 0.6F;
    private static final float RAY_FALLOFF = 2.2F;
    private static final float RAY_SHARP = 3.5F;
    private static final float DRIFT = 0.004F;
    private static final int BUFFER = SEGMENTS * RINGS * 2 * 4
            * DefaultVertexFormat.POSITION_COLOR.getVertexSize();

    private static final RenderType GLOW = RenderType.create("caldarium_corona",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, BUFFER, false, true,
            RenderType.CompositeState.builder()
                    // ⚠ position_color has no fog at all. Lightning takes the same vertices
                    // and fades them with the distance, so the corona goes with the ball.
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                    .createCompositeState(false));

    private static final List<Pending> PENDING = new ArrayList<>();

    private record Pending(Vec3 centre, float radius, float blaze, float time, long seed) {
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
        return BLAZING_RAY_REACH;
    }

    public static int segments() {
        return SEGMENTS;
    }

    public static void queue(Vec3 centre, float radius, float blaze, float time, long seed) {
        PENDING.add(new Pending(centre, radius, blaze, time, seed));
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
            int colour = SolPalette.glow(pending.blaze());
            float phase = pending.time() * DRIFT + Math.floorMod(pending.seed(), 1000L);
            ring(pose, into, offset, limb, colour, Mth.lerp(pending.blaze(), HALO_ALPHA, BLAZING_HALO_ALPHA),
                    Mth.lerp(pending.blaze(), HALO_REACH, BLAZING_HALO_REACH), HALO_FALLOFF, phase, false);
            ring(pose, into, offset, limb, colour, Mth.lerp(pending.blaze(), RAY_ALPHA, BLAZING_RAY_ALPHA),
                    Mth.lerp(pending.blaze(), RAY_REACH, BLAZING_RAY_REACH), RAY_FALLOFF, phase, true);
        }
        PENDING.clear();
        buffers.endBatch(GLOW);
    }

    private static void ring(PoseStack.Pose pose, VertexConsumer into, Vec3 offset, Limb limb, int colour,
            float alpha, float reach, float falloff, float phase, boolean rays) {
        for (int i = 0; i < SEGMENTS; i++) {
            float from = Mth.TWO_PI * i / SEGMENTS;
            float to = Mth.TWO_PI * (i + 1) / SEGMENTS;
            double out = limb.edge() * reach;
            float here = alpha * (rays ? ray(from, phase) : 1.0F);
            float next = alpha * (rays ? ray(to, phase) : 1.0F);
            for (int step = 0; step < RINGS; step++) {
                float near = step / (float) RINGS;
                float far = (step + 1) / (float) RINGS;
                shine(pose, into, offset.add(limb.at(from, span(limb, out, near))), colour, fade(here, near, falloff));
                shine(pose, into, offset.add(limb.at(from, span(limb, out, far))), colour, fade(here, far, falloff));
                shine(pose, into, offset.add(limb.at(to, span(limb, out, far))), colour, fade(next, far, falloff));
                shine(pose, into, offset.add(limb.at(to, span(limb, out, near))), colour, fade(next, near, falloff));
            }
        }
    }

    // ⚠ Whole turns only: a fraction here leaves the wave out of step with itself
    // where the ring closes, which shows as a seam down one side of the corona.
    private static float ray(float angle, float phase) {
        float wave = Mth.sin(angle * 4.0F + phase + 1.7F) * 0.45F
                + Mth.sin(angle * 9.0F - phase * 1.3F - 0.9F) * 0.33F
                + Mth.sin(angle * 19.0F + phase * 0.7F + 2.4F) * 0.22F;
        return (float) Math.pow((wave + 1.0F) * 0.5F, RAY_SHARP);
    }

    private static double span(Limb limb, double reach, float at) {
        return Mth.lerp(at, limb.outer(), reach);
    }

    private static float fade(float alpha, float at, float falloff) {
        return alpha * (float) Math.pow(1.0F - at, falloff);
    }

    private static void shine(PoseStack.Pose pose, VertexConsumer into, Vec3 point, int colour, float alpha) {
        into.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(SolPalette.channel(colour, 16), SolPalette.channel(colour, 8),
                        SolPalette.channel(colour, 0), alpha);
    }
}
