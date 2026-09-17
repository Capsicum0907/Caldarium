package io.github.capsicum0907.caldarium.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.github.capsicum0907.caldarium.Caldarium;
import io.github.capsicum0907.caldarium.SolPalette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class SolSurface {
    private static final float REFERENCE_RADIUS = 4.0F;
    private static final float TEXEL = 0.3F;
    private static final int MIN_TEXELS = 16;
    private static final int MAX_TEXELS = 160;
    private static final int GRID = 16;
    private static final int FACES = 6;
    private static final int COLUMNS = 3;
    private static final int SHADES = 256;
    private static final float QUARTER = Mth.PI / 4.0F;
    private static final Vector3f LIT = new Vector3f(0.0F, 1.0F, 0.0F);

    private static final float[][] NORMAL = {
            { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 } };
    private static final float[][] ACROSS = {
            { 0, 0, -1 }, { 0, 0, 1 }, { 1, 0, 0 }, { 1, 0, 0 }, { 1, 0, 0 }, { -1, 0, 0 } };
    private static final float[][] UP = {
            { 0, 1, 0 }, { 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 }, { 0, 1, 0 }, { 0, 1, 0 } };

    private static int made;

    private final ResourceLocation location;
    private final DynamicTexture texture;
    private final NativeImage image;
    private final int texels;
    private final int cell;
    private final float scale;
    private final float[][] heat;
    private final int[] shades = new int[SHADES];
    private int nextFace;
    private int nextRow;
    private float drawnAt = Float.NaN;
    private boolean filled;

    public SolSurface(float radius, float scale) {
        this.texels = Mth.clamp(Mth.ceil(radius * QUARTER * 2.0F / TEXEL), MIN_TEXELS, MAX_TEXELS);
        this.cell = texels + 2;
        this.scale = scale;
        this.heat = new float[FACES][cell * cell];
        this.image = new NativeImage(NativeImage.Format.RGBA, cell * COLUMNS, cell * (FACES / COLUMNS), false);
        this.texture = new DynamicTexture(image);
        this.texture.setFilter(true, false);
        this.location = ResourceLocation.fromNamespaceAndPath(Caldarium.MODID, "dynamic/sol_" + made++);
        Minecraft.getInstance().getTextureManager().register(location, texture);
    }

    public static float scaleFor(float radius) {
        return radius / REFERENCE_RADIUS;
    }

    public boolean fits(float radius, float scale) {
        return this.scale == scale
                && texels == Mth.clamp(Mth.ceil(radius * QUARTER * 2.0F / TEXEL), MIN_TEXELS, MAX_TEXELS);
    }

    public ResourceLocation location() {
        return location;
    }

    public void release() {
        Minecraft.getInstance().getTextureManager().release(location);
    }

    public void refresh(float time, float boil, float blaze) {
        if (time == drawnAt) {
            return;
        }
        drawnAt = time;
        if (!filled) {
            for (int face = 0; face < FACES; face++) {
                sample(face, 0, cell, boil);
            }
            filled = true;
        } else {
            int rows = Math.min(cell - nextRow, (cell + 1) / 2);
            sample(nextFace, nextRow, nextRow + rows, boil);
            nextRow += rows;
            if (nextRow >= cell) {
                nextRow = 0;
                nextFace = (nextFace + 1) % FACES;
            }
        }
        for (int shade = 0; shade < SHADES; shade++) {
            int rgb = SolPalette.colour(shade / (float) (SHADES - 1), blaze);
            shades[shade] = 0xFF000000 | (rgb & 0xFF) << 16 | (rgb & 0xFF00) | (rgb >> 16) & 0xFF;
        }
        for (int face = 0; face < FACES; face++) {
            int left = (face % COLUMNS) * cell;
            int top = (face / COLUMNS) * cell;
            float[] values = heat[face];
            for (int j = 0; j < cell; j++) {
                for (int i = 0; i < cell; i++) {
                    int shade = Math.round(values[j * cell + i] * (SHADES - 1));
                    image.setPixelRGBA(left + i, top + j, shades[shade]);
                }
            }
        }
        texture.upload();
    }

    private void sample(int face, int fromRow, int toRow, float boil) {
        float[] values = heat[face];
        Vector3f point = new Vector3f();
        for (int j = fromRow; j < toRow; j++) {
            float t = ((j - 1) + 0.5F) / texels * 2.0F - 1.0F;
            for (int i = 0; i < cell; i++) {
                float s = ((i - 1) + 0.5F) / texels * 2.0F - 1.0F;
                onBall(face, s, t, point);
                values[j * cell + i] = Sunspots.heat(point.x * scale, point.y * scale, point.z * scale, boil);
            }
        }
    }

    private static void onBall(int face, float s, float t, Vector3f into) {
        float a = (float) Math.tan(s * QUARTER);
        float b = (float) Math.tan(t * QUARTER);
        float[] n = NORMAL[face];
        float[] u = ACROSS[face];
        float[] v = UP[face];
        into.set(n[0] + u[0] * a + v[0] * b, n[1] + u[1] * a + v[1] * b, n[2] + u[2] * a + v[2] * b).normalize();
    }

    public void draw(PoseStack pose, VertexConsumer into, int overlay, boolean inside, boolean flat) {
        Matrix4f matrix = pose.last().pose();
        Vector3f point = new Vector3f();
        float width = cell * COLUMNS;
        float height = cell * (FACES / COLUMNS);
        for (int face = 0; face < FACES; face++) {
            float left = (face % COLUMNS) * cell + 1;
            float top = (face / COLUMNS) * cell + 1;
            for (int j = 0; j < GRID; j++) {
                float t0 = j / (float) GRID * 2.0F - 1.0F;
                float t1 = (j + 1) / (float) GRID * 2.0F - 1.0F;
                for (int i = 0; i < GRID; i++) {
                    float s0 = i / (float) GRID * 2.0F - 1.0F;
                    float s1 = (i + 1) / (float) GRID * 2.0F - 1.0F;
                    float first = inside ? s1 : s0;
                    float last = inside ? s0 : s1;
                    corner(matrix, pose, into, overlay, face, first, t0, left, top, width, height, point, flat);
                    corner(matrix, pose, into, overlay, face, last, t0, left, top, width, height, point, flat);
                    corner(matrix, pose, into, overlay, face, last, t1, left, top, width, height, point, flat);
                    corner(matrix, pose, into, overlay, face, first, t1, left, top, width, height, point, flat);
                }
            }
        }
    }

    private void corner(Matrix4f matrix, PoseStack pose, VertexConsumer into, int overlay, int face,
            float s, float t, float left, float top, float width, float height, Vector3f point, boolean flat) {
        onBall(face, s, t, point);
        float u = (left + (s + 1.0F) / 2.0F * texels) / width;
        float v = (top + (t + 1.0F) / 2.0F * texels) / height;
        VertexConsumer vertex = into.addVertex(matrix, point.x, point.y, point.z)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(LightTexture.FULL_BRIGHT);
        if (flat) {
            vertex.setNormal(LIT.x, LIT.y, LIT.z);
        } else {
            vertex.setNormal(pose.last(), point.x, point.y, point.z);
        }
    }
}
