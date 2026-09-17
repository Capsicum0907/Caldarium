package io.github.capsicum0907.caldarium;

public final class SolPalette {
    private static final int[] STOPS = { 0x6E1606, 0xC0300A, 0xE8601A, 0xFA9A24, 0xFFE070 };
    private static final double CONTRAST = 1.3;

    public static final int RIM = 0x4A0A03;
    public static final int GLOW = 0xFF6A10;

    private SolPalette() {
    }

    public static int colour(float heat) {
        float scaled = (float) Math.pow(Math.clamp(heat, 0.0F, 1.0F), CONTRAST) * (STOPS.length - 1);
        int stop = Math.min((int) scaled, STOPS.length - 2);
        return mix(STOPS[stop], STOPS[stop + 1], scaled - stop);
    }

    public static float channel(int colour, int shift) {
        return ((colour >> shift) & 0xFF) / 255.0F;
    }

    private static int mix(int from, int to, float at) {
        int mixed = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            int a = (from >> shift) & 0xFF;
            int b = (to >> shift) & 0xFF;
            mixed |= Math.round(a + (b - a) * at) << shift;
        }
        return mixed;
    }
}
