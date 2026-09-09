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
    /**
     * A blank to hang the vertex colours on.
     *
     * <p>The surface is not a picture: {@link Sunspots} answers what colour a point of
     * it is from where that point is. One white pixel is all the render type needs.
     */
    private static final ResourceLocation SKIN = ResourceLocation
            .fromNamespaceAndPath(Caldarium.MODID, "textures/block/" + Skins.SOL_LIT + ".png");

    /** Bands around the sphere, and segments around each band. */
    private static final int RINGS = 32;
    private static final int SEGMENTS = 64;

    /** How fast the surface churns, in noise units a second. */
    private static final float BOIL = 0.35F;

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
        float time = (sol.getLevel() == null ? 0L : sol.getLevel().getGameTime()) + partial;
        float spin = time * TURN / 20.0F;
        float boil = time * BOIL / 20.0F;

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        pose.mulPose(Axis.XP.rotationDegrees(spin * 0.37F));
        pose.scale(radius, radius, radius);

        // ⚠ Solid rather than translucent emissive. That one draws with NO_CULL and a
        // COLOR_WRITE mask - no back face culling and no depth written - so the far
        // half of the ball comes through the near half and the pattern doubles. The
        // light does not come from the render type anyway: it comes from handing every
        // vertex FULL_BRIGHT, which this one takes just as happily.
        VertexConsumer into = buffers.getBuffer(RenderType.entitySolid(SKIN));
        ball(pose, into, overlay, boil);
        pose.popPose();
    }

    /**
     * A sphere of quads. Every vertex is drawn at full brightness, which is what makes
     * it a light rather than a lit thing: a sun that dimmed in shadow would be a ball.
     */
    private static void ball(PoseStack pose, VertexConsumer into, int overlay, float boil) {
        Matrix4f matrix = pose.last().pose();
        for (int ring = 0; ring < RINGS; ring++) {
            float from = Mth.PI * ring / RINGS;
            float to = Mth.PI * (ring + 1) / RINGS;
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float left = Mth.TWO_PI * segment / SEGMENTS;
                float right = Mth.TWO_PI * (segment + 1) / SEGMENTS;
                corner(matrix, pose, into, overlay, from, left, boil);
                corner(matrix, pose, into, overlay, to, left, boil);
                corner(matrix, pose, into, overlay, to, right, boil);
                corner(matrix, pose, into, overlay, from, right, boil);
            }
        }
    }

    /** The seam colour, and the two it climbs through as a spot heats up. */
    private static final int COOL = 0xC2400C;
    private static final int WARM = 0xF9A11B;
    private static final int HOT = 0xFFF6D8;

    private static void corner(Matrix4f matrix, PoseStack pose, VertexConsumer into,
            int overlay, float down, float round, float boil) {
        float x = Mth.sin(down) * Mth.cos(round);
        float y = Mth.cos(down);
        float z = Mth.sin(down) * Mth.sin(round);
        float heat = Sunspots.heat(x, y, z, boil);
        int colour = heat < 0.5F
                ? blend(COOL, WARM, heat * 2.0F)
                : blend(WARM, HOT, (heat - 0.5F) * 2.0F);
        Vector3f normal = new Vector3f(x, y, z);
        into.addVertex(matrix, x, y, z)
                .setColor(colour | 0xFF000000)
                .setUv(0.5F, 0.5F)
                .setOverlay(overlay)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose.last(), normal.x, normal.y, normal.z);
    }

    private static int blend(int from, int to, float at) {
        int r = Math.round(Mth.lerp(at, (from >> 16) & 0xFF, (to >> 16) & 0xFF));
        int g = Math.round(Mth.lerp(at, (from >> 8) & 0xFF, (to >> 8) & 0xFF));
        int b = Math.round(Mth.lerp(at, from & 0xFF, to & 0xFF));
        return (r << 16) | (g << 8) | b;
    }
}
