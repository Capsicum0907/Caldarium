package io.github.capsicum0907.caldarium;

public final class SolPalette {
    private static final int[] STOPS = { 0xA81200, 0xE03800, 0xFF7800, 0xFFB414, 0xFFE258 };
    private static final double CONTRAST = 0.9;
    private static final int[] BLAZING = { 0xF04000, 0xFF8C00, 0xFFC814, 0xFFE850, 0xFFF9A0 };
    private static final double BLAZING_CONTRAST = 0.8;

    public static final int RIM = 0x961000;
    public static final int GLOW = 0xFF6A10;
    public static final int BLAZING_GLOW = 0xFFB030;

    private SolPalette() {
    }

    public static int colour(float heat, float blaze) {
        return mix(along(STOPS, CONTRAST, heat), along(BLAZING, BLAZING_CONTRAST, heat), blaze);
    }

    public static int glow(float blaze) {
        return mix(GLOW, BLAZING_GLOW, blaze);
    }

    public static float blaze(float time, long seed) {
        if (!CaldariumConfig.SPEC.isLoaded()) {
            return 0.0F;
        }
        int every = Math.max(1, CaldariumConfig.solPulseEvery());
        int length = Math.min(CaldariumConfig.solPulseLength(), every);
        if (length <= 0) {
            return 0.0F;
        }
        float into = (float) ((time + Math.floorMod(seed, every)) % every);
        if (into >= length) {
            return 0.0F;
        }
        float wave = (float) Math.sin(Math.PI * into / length);
        return wave * wave;
    }

    public static float channel(int colour, int shift) {
        return ((colour >> shift) & 0xFF) / 255.0F;
    }

    private static int along(int[] stops, double contrast, float heat) {
        float scaled = (float) Math.pow(Math.clamp(heat, 0.0F, 1.0F), contrast) * (stops.length - 1);
        int stop = Math.min((int) scaled, stops.length - 2);
        return mix(stops[stop], stops[stop + 1], scaled - stop);
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
