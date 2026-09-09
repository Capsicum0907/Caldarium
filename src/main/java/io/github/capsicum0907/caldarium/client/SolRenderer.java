package io.github.capsicum0907.caldarium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import io.github.capsicum0907.caldarium.Caldarium;
import io.github.capsicum0907.caldarium.CaldariumConfig;
import io.github.capsicum0907.caldarium.SolBlockEntity;
import io.github.capsicum0907.caldarium.data.Skins;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SolRenderer implements BlockEntityRenderer<SolBlockEntity> {
    private static final ResourceLocation SKIN = ResourceLocation
            .fromNamespaceAndPath(Caldarium.MODID, "textures/block/" + Skins.SOL + ".png");

    /** Bands around the sphere, and segments around each band. */
    private static final int RINGS = 20;
    private static final int SEGMENTS = 32;

    /** Degrees a second. Slow enough to read as churning rather than spinning. */
    private static final float TURN = 6.0F;

    /** How much of its size a sun has left when it is nearly out. */
    private static final float SHRUNK = 0.55F;

    public SolRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SolBlockEntity sol) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(SolBlockEntity sol, float partial, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        float radius = CaldariumConfig.solSize() * (SHRUNK + (1.0F - SHRUNK) * sol.share());
        float spin = (sol.getLevel() == null ? 0 : sol.getLevel().getGameTime() + partial)
                * TURN / 20.0F;

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        pose.mulPose(Axis.XP.rotationDegrees(spin * 0.37F));
        pose.scale(radius, radius, radius);

        VertexConsumer into = buffers.getBuffer(RenderType.entityTranslucentEmissive(SKIN));
        ball(pose, into, overlay);
        pose.popPose();
    }

    /**
     * A sphere of quads. Every vertex is drawn at full brightness, which is what makes
     * it a light rather than a lit thing: a sun that dimmed in shadow would be a ball.
     */
    private static void ball(PoseStack pose, VertexConsumer into, int overlay) {
        Matrix4f matrix = pose.last().pose();
        for (int ring = 0; ring < RINGS; ring++) {
            float from = Mth.PI * ring / RINGS;
            float to = Mth.PI * (ring + 1) / RINGS;
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float left = Mth.TWO_PI * segment / SEGMENTS;
                float right = Mth.TWO_PI * (segment + 1) / SEGMENTS;
                corner(matrix, pose, into, overlay, from, left, ring, segment);
                corner(matrix, pose, into, overlay, to, left, ring + 1, segment);
                corner(matrix, pose, into, overlay, to, right, ring + 1, segment + 1);
                corner(matrix, pose, into, overlay, from, right, ring, segment + 1);
            }
        }
    }

    private static void corner(Matrix4f matrix, PoseStack pose, VertexConsumer into,
            int overlay, float down, float round, int ring, int segment) {
        float x = Mth.sin(down) * Mth.cos(round);
        float y = Mth.cos(down);
        float z = Mth.sin(down) * Mth.sin(round);
        Vector3f normal = new Vector3f(x, y, z);
        into.addVertex(matrix, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                .setUv((float) segment / SEGMENTS, (float) ring / RINGS)
                .setOverlay(overlay)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose.last(), normal.x, normal.y, normal.z);
    }
}
