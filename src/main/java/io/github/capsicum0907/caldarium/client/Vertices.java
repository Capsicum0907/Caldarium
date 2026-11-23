package io.github.capsicum0907.caldarium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix4f;

public final class Vertices {
    private Vertices() {
    }

    public static void line(VertexConsumer into, PoseStack.Pose pose, float x, float y, float z,
            float red, float green, float blue, float alpha, float normalX, float normalY, float normalZ) {
        //? if >=1.21 {
        into.addVertex(pose, x, y, z).setColor(red, green, blue, alpha).setNormal(pose, normalX, normalY, normalZ);
        //?} else
        //into.vertex(pose.pose(), x, y, z).color(red, green, blue, alpha).normal(pose.normal(), normalX, normalY, normalZ).endVertex();
    }

    public static void coloured(VertexConsumer into, PoseStack.Pose pose, float x, float y, float z,
            float red, float green, float blue, float alpha) {
        //? if >=1.21 {
        into.addVertex(pose, x, y, z).setColor(red, green, blue, alpha);
        //?} else
        //into.vertex(pose.pose(), x, y, z).color(red, green, blue, alpha).endVertex();
    }

    public static void lit(VertexConsumer into, Matrix4f matrix, float x, float y, float z, int argb,
            float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        //? if >=1.21 {
        into.addVertex(matrix, x, y, z).setColor(argb).setUv(u, v).setOverlay(overlay).setLight(light)
                .setNormal(normalX, normalY, normalZ);
        //?} else
        //into.vertex(matrix, x, y, z).color(argb).uv(u, v).overlayCoords(overlay).uv2(light).normal(normalX, normalY, normalZ).endVertex();
    }

    public static void lit(VertexConsumer into, Matrix4f matrix, PoseStack.Pose normals, float x, float y,
            float z, int argb, float u, float v, int overlay, int light, float normalX, float normalY,
            float normalZ) {
        //? if >=1.21 {
        into.addVertex(matrix, x, y, z).setColor(argb).setUv(u, v).setOverlay(overlay).setLight(light)
                .setNormal(normals, normalX, normalY, normalZ);
        //?} else
        //into.vertex(matrix, x, y, z).color(argb).uv(u, v).overlayCoords(overlay).uv2(light).normal(normals.normal(), normalX, normalY, normalZ).endVertex();
    }
}
