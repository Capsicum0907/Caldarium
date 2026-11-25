package io.github.capsicum0907.caldarium.client;

import java.util.Map;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
//? if >=1.19.3
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
//? if >=1.17
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


//? if >=1.17 {
public class SolRenderer implements BlockEntityRenderer<SolBlockEntity> {
//?} else
/*public class SolRenderer extends BlockEntityRenderer<SolBlockEntity> {*/
    private static final ResourceLocation SKIN = Caldarium.id("textures/block/" + Skins.SOL_LIT + ".png");

    /** How fast the surface churns, in noise units a second. */
    private static final float BOIL = 0.35F;

    /** Degrees a second. Slow enough to read as churning rather than spinning. */
    private static final float TURN = 6.0F;

    // Straight up faces both world diffuse lights, so the shader's shading comes out at full.
    private static final Point LIT = new Point(0.0F, 1.0F, 0.0F);

    private static final Map<SolBlockEntity, SolSurface> SURFACES = new WeakHashMap<>();

    public static final int VIEW = 256;

    //? if >=1.17 {
    public SolRenderer(BlockEntityRendererProvider.Context context) {
    }
    //?} else {
    /*public SolRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }
    *///?}

    public static void forget(SolBlockEntity sol) {
        SolSurface surface = SURFACES.remove(sol);
        if (surface != null) {
            surface.release();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(SolBlockEntity sol) {
        return true;
    }

    //? if neoforge {
    @Override
    public AABB getRenderBoundingBox(SolBlockEntity sol) {
        return bounds(sol);
    }
    //?}

    public static AABB bounds(SolBlockEntity sol) {
        double reach = SolBlock.radius(sol.getBlockState()) * SolCorona.reach();
        return new AABB(SolBlock.centre(sol.getBlockPos()), SolBlock.centre(sol.getBlockPos())).inflate(reach);
    }

    //? if >=1.17 {
    @Override
    public int getViewDistance() {
        return VIEW;
    }
    //?}

    @Override
    public void render(SolBlockEntity sol, float partial, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        float radius = SolBlock.radius(sol.getBlockState());
        float scale = SolSurface.scaleFor(radius);
        SolSurface surface = SURFACES.get(sol);
        if (surface == null || !surface.fits(radius, scale)) {
            if (surface != null) {
                surface.release();
            }
            surface = new SolSurface(radius, scale);
            SURFACES.put(sol, surface);
        }
        float time = (sol.getLevel() == null ? 0L : sol.getLevel().getGameTime()) + partial;
        Vec3 eye = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float blaze = SolPalette.blaze(time, sol.getBlockPos().asLong());
        Vec3 centre = SolBlock.centre(sol.getBlockPos());
        Vec3 toCamera = eye.subtract(centre);
        if (toCamera.length() >= radius) {
            SolCorona.queue(centre, radius, blaze, time, sol.getBlockPos().asLong());
        }
        draw(pose, buffers, overlay, radius, time, blaze, toCamera, surface);
    }

    public static void draw(PoseStack pose, MultiBufferSource buffers, int overlay, float radius, float time,
            float blaze, Vec3 toCamera, SolSurface surface) {
        float spin = time * TURN / 20.0F;
        float boil = time * BOIL / 20.0F;
        boolean inside = toCamera != null && toCamera.length() < radius;
        surface.refresh(time, boil, blaze);

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        if (toCamera != null && !inside) {
            halo(pose, buffers, overlay, radius, toCamera);
        }
        //? if >=1.19.3 {
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        pose.mulPose(Axis.XP.rotationDegrees(spin * 0.37F));
        //?} else {
        /*pose.mulPose(com.mojang.math.Vector3f.YP.rotationDegrees(spin));
        pose.mulPose(com.mojang.math.Vector3f.XP.rotationDegrees(spin * 0.37F));
        *///?}
        pose.scale(radius, radius, radius);

        // ⚠ Solid rather than translucent emissive: that one skips back-face culling and
        // depth writes, so the far half of the ball shows through the near half.
        VertexConsumer into = buffers.getBuffer(RenderType.entitySolid(surface.location()));
        surface.draw(pose, into, overlay, inside, toCamera != null);
        pose.popPose();
    }

    private static void halo(PoseStack pose, MultiBufferSource buffers, int overlay, float radius, Vec3 toCamera) {
        SolCorona.Limb limb = SolCorona.Limb.of(toCamera, radius);
        PoseStack.Pose at = pose.last();
        VertexConsumer solid = buffers.getBuffer(RenderType.entitySolid(SKIN));
        int segments = SolCorona.segments();
        for (int i = 0; i < segments; i++) {
            float from = Mth.TWO_PI * i / segments;
            float to = Mth.TWO_PI * (i + 1) / segments;
            rim(at, solid, overlay, limb.at(from, limb.inner()));
            rim(at, solid, overlay, limb.at(from, limb.outer()));
            rim(at, solid, overlay, limb.at(to, limb.outer()));
            rim(at, solid, overlay, limb.at(to, limb.inner()));
        }
    }

    private static void rim(PoseStack.Pose at, VertexConsumer into, int overlay, Vec3 point) {
        Vertices.lit(into, at.pose(), (float) point.x, (float) point.y, (float) point.z,
                SolPalette.RIM | 0xFF000000, 0.5F, 0.5F, overlay, LightTexture.FULL_BRIGHT,
                LIT.x, LIT.y, LIT.z);
    }
}
