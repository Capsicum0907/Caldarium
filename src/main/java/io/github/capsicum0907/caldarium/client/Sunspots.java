package io.github.capsicum0907.caldarium.client;

import net.minecraft.util.Mth;

/**
 * How hot a point on the sun's surface is, asked in three dimensions.
 *
 * <p>⚠ Not a picture wrapped round a ball. A flat picture on a sphere pinches to a
 * point at both poles and stretches its pattern along the lines of latitude, which is
 * a mirror ball rather than a sun, and no amount of redrawing the picture fixes it.
 * Asking the surface where it is instead has neither fault: there is no seam, no pole
 * and no grid, because there is nothing being wrapped.
 */
public final class Sunspots {
    private Sunspots() {
    }

    public static float heat(float x, float y, float z, float time) {
        float v = noise(x * 3.1F + time * 0.35F, y * 3.1F, z * 3.1F + time * 0.2F, 1) * 0.55F;
        v += noise(x * 6.7F, y * 6.7F + time * 0.5F, z * 6.7F, 2) * 0.28F;
        v += noise(x * 13.0F + time * 0.7F, y * 13.0F, z * 13.0F, 3) * 0.17F;
        return Mth.clamp((v - 0.32F) * 2.4F, 0.0F, 1.0F);
    }

    private static float noise(float x, float y, float z, int salt) {
        int i = Mth.floor(x);
        int j = Mth.floor(y);
        int k = Mth.floor(z);
        float sx = smooth(x - i);
        float sy = smooth(y - j);
        float sz = smooth(z - k);
        float x00 = lerp(at(i, j, k, salt), at(i + 1, j, k, salt), sx);
        float x10 = lerp(at(i, j + 1, k, salt), at(i + 1, j + 1, k, salt), sx);
        float x01 = lerp(at(i, j, k + 1, salt), at(i + 1, j, k + 1, salt), sx);
        float x11 = lerp(at(i, j + 1, k + 1, salt), at(i + 1, j + 1, k + 1, salt), sx);
        return lerp(lerp(x00, x10, sy), lerp(x01, x11, sy), sz);
    }

    private static float smooth(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    private static float lerp(float from, float to, float at) {
        return from + (to - from) * at;
    }

    private static float at(int x, int y, int z, int salt) {
        int n = x * 374761393 + y * 668265263 + z * 1274126177 + salt * -1640531527;
        n = (n ^ (n >>> 13)) * 1274126177;
        return ((n ^ (n >>> 16)) & 0xFFFF) / 65535.0F;
    }
}
