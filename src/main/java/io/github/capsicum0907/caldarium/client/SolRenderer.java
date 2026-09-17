package io.github.capsicum0907.caldarium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import io.github.capsicum0907.caldarium.Caldarium;
import io.github.capsicum0907.caldarium.SolBlock;
import io.github.capsicum0907.caldarium.SolBlockEntity;
import io.github.capsicum0907.caldarium.SolPalette;
import io.github.capsicum0907.caldarium.data.Skins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import net.minecraft.world.phys.Vec3;

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

    private static final int HALO_SEGMENTS = 96;
    // Straight up faces both world diffuse lights, so the shader's shading comes out at full.
    private static final Vector3f LIT = new Vector3f(0.0F, 1.0F, 0.0F);
    private static final float RIM_INSIDE = 0.98F;
    private static final float RIM_OUTSIDE = 1.025F;
    private static final float CORONA = 1.3F;
    private static final float BLAZING_CORONA = 1.6F;
    private static final float GLOW_ALPHA = 0.55F;
    private static final float BLAZING_GLOW_ALPHA = 0.8F;

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
        float radius = SolBlock.radius(sol.getBlockState());
        float time = (sol.getLevel() == null ? 0L : sol.getLevel().getGameTime()) + partial;
        Vec3 eye = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float blaze = SolPalette.blaze(time, sol.getBlockPos().asLong());
        draw(pose, buffers, overlay, radius, time, blaze, eye.subtract(SolBlock.centre(sol.getBlockPos())));
    }

    public static void draw(PoseStack pose, MultiBufferSource buffers, int overlay, float radius, float time,
            float blaze, Vec3 toCamera) {
        float spin = time * TURN / 20.0F;
        float boil = time * BOIL / 20.0F;
        boolean inside = toCamera != null && toCamera.length() < radius;

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        if (toCamera != null && !inside) {
            halo(pose, buffers, overlay, radius, blaze, toCamera);
        }
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        pose.mulPose(Axis.XP.rotationDegrees(spin * 0.37F));
        pose.scale(radius, radius, radius);

        // ⚠ Solid rather than translucent emissive. That one draws with NO_CULL and a
        // COLOR_WRITE mask - no back face culling and no depth written - so the far
        // half of the ball comes through the near half and the pattern doubles. The
        // light does not come from the render type anyway: it comes from handing every
        // vertex FULL_BRIGHT, which this one takes just as happily.
        VertexConsumer into = buffers.getBuffer(RenderType.entitySolid(SKIN));
        ball(pose, into, overlay, boil, blaze, inside, toCamera != null);
        pose.popPose();
    }

    /**
     * A sphere of quads. Every vertex is drawn at full brightness, which is what makes
     * it a light rather than a lit thing: a sun that dimmed in shadow would be a ball.
     */
    private static void ball(PoseStack pose, VertexConsumer into, int overlay, float boil, float blaze,
            boolean inside, boolean flat) {
        Matrix4f matrix = pose.last().pose();
        for (int ring = 0; ring < RINGS; ring++) {
            float from = Mth.PI * ring / RINGS;
            float to = Mth.PI * (ring + 1) / RINGS;
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float left = Mth.TWO_PI * segment / SEGMENTS;
                float right = Mth.TWO_PI * (segment + 1) / SEGMENTS;
                float first = inside ? right : left;
                float last = inside ? left : right;
                corner(matrix, pose, into, overlay, from, first, boil, blaze, flat);
                corner(matrix, pose, into, overlay, from, last, boil, blaze, flat);
                corner(matrix, pose, into, overlay, to, last, boil, blaze, flat);
                corner(matrix, pose, into, overlay, to, first, boil, blaze, flat);
            }
        }
    }

    private static void halo(PoseStack pose, MultiBufferSource buffers, int overlay, float radius, float blaze,
            Vec3 toCamera) {
        float corona = Mth.lerp(blaze, CORONA, BLAZING_CORONA);
        float alpha = Mth.lerp(blaze, GLOW_ALPHA, BLAZING_GLOW_ALPHA);
        int colour = SolPalette.glow(blaze);
        double distance = toCamera.length();
        Vec3 towards = toCamera.scale(1.0 / distance);
        Vec3 facing = towards.reverse();
        Vec3 up = Math.abs(towards.y) > 0.99 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 across = facing.cross(up).normalize();
        Vec3 upward = across.cross(facing);
        Vec3 plane = towards.scale(radius * radius / distance);
        double edge = radius * Math.sqrt(1.0 - (radius / distance) * (radius / distance));

        PoseStack.Pose at = pose.last();
        VertexConsumer solid = buffers.getBuffer(RenderType.entitySolid(SKIN));
        for (int i = 0; i < HALO_SEGMENTS; i++) {
            Vec3 a = direction(across, upward, Mth.TWO_PI * i / HALO_SEGMENTS);
            Vec3 b = direction(across, upward, Mth.TWO_PI * (i + 1) / HALO_SEGMENTS);
            rim(at, solid, overlay, plane.add(a.scale(edge * RIM_INSIDE)), towards);
            rim(at, solid, overlay, plane.add(a.scale(edge * RIM_OUTSIDE)), towards);
            rim(at, solid, overlay, plane.add(b.scale(edge * RIM_OUTSIDE)), towards);
            rim(at, solid, overlay, plane.add(b.scale(edge * RIM_INSIDE)), towards);
        }

        VertexConsumer glow = buffers.getBuffer(RenderType.debugQuads());
        for (int i = 0; i < HALO_SEGMENTS; i++) {
            Vec3 a = direction(across, upward, Mth.TWO_PI * i / HALO_SEGMENTS);
            Vec3 b = direction(across, upward, Mth.TWO_PI * (i + 1) / HALO_SEGMENTS);
            shine(at, glow, plane.add(a.scale(edge * RIM_OUTSIDE)), colour, alpha);
            shine(at, glow, plane.add(a.scale(edge * corona)), colour, 0.0F);
            shine(at, glow, plane.add(b.scale(edge * corona)), colour, 0.0F);
            shine(at, glow, plane.add(b.scale(edge * RIM_OUTSIDE)), colour, alpha);
        }
    }

    private static Vec3 direction(Vec3 across, Vec3 upward, float angle) {
        return across.scale(Mth.cos(angle)).add(upward.scale(Mth.sin(angle)));
    }

    private static void rim(PoseStack.Pose at, VertexConsumer into, int overlay, Vec3 point, Vec3 normal) {
        into.addVertex(at, (float) point.x, (float) point.y, (float) point.z)
                .setColor(SolPalette.RIM | 0xFF000000)
                .setUv(0.5F, 0.5F)
                .setOverlay(overlay)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(LIT.x, LIT.y, LIT.z);
    }

    private static void shine(PoseStack.Pose at, VertexConsumer into, Vec3 point, int colour, float alpha) {
        into.addVertex(at, (float) point.x, (float) point.y, (float) point.z)
                .setColor(SolPalette.channel(colour, 16), SolPalette.channel(colour, 8),
                        SolPalette.channel(colour, 0), alpha);
    }

    private static void corner(Matrix4f matrix, PoseStack pose, VertexConsumer into,
            int overlay, float down, float round, float boil, float blaze, boolean flat) {
        float x = Mth.sin(down) * Mth.cos(round);
        float y = Mth.cos(down);
        float z = Mth.sin(down) * Mth.sin(round);
        float heat = Sunspots.heat(x, y, z, boil);
        int colour = SolPalette.colour(heat, blaze);
        VertexConsumer vertex = into.addVertex(matrix, x, y, z)
                .setColor(colour | 0xFF000000)
                .setUv(0.5F, 0.5F)
                .setOverlay(overlay)
                .setLight(LightTexture.FULL_BRIGHT);
        if (flat) {
            vertex.setNormal(LIT.x, LIT.y, LIT.z);
        } else {
            vertex.setNormal(pose.last(), x, y, z);
        }
    }

}
