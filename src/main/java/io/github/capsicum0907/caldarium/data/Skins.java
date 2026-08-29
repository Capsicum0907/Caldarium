package io.github.capsicum0907.caldarium.data;

import java.util.ArrayList;
import java.util.List;

import io.github.capsicum0907.caldarium.Generator;
import io.github.capsicum0907.caldarium.Tier;

/**
 * Every texture the mod has, as a formula. No PNG is kept in the repository.
 *
 * <p><b>The palette is here and nowhere else.</b> A colour written into a painting
 * routine is a colour that has to be found again to change, so the constants below
 * are the one place the mod says what it looks like, and everything underneath takes
 * them as arguments.
 *
 * <p>A picture is a plate of metal with a window in it. What the window shows is the
 * only difference between the blocks, which is the point: they are one machine seen
 * doing two jobs.
 */
public final class Skins {
    public static final int SIZE = 16;

    private static final int BODY = 0x8A8F94;
    private static final int EDGE = 0x51565A;
    private static final int RIVET = 0xA8AEB3;

    private static final int MOUTH_COLD = 0x2B2724;
    private static final int MOUTH_LIT = 0xFF9A2E;
    private static final int EMBER = 0xC8461B;

    /** One per rung of the ladder, taken by ordinal; the last one repeats if it runs out. */
    private static final int[] CELL = { 0xD5DBE0 };

    private static final int WINDOW_FROM = 4;
    private static final int WINDOW_TO = 12;

    private Skins() {
    }

    /** The name of the texture for a generator, unlit or burning. */
    public static String generator(Generator generator, boolean lit) {
        return lit ? generator.id() + "_on" : generator.id();
    }

    public static String battery(Tier tier) {
        return tier.batteryId();
    }

    /**
     * The names this will write. The model provider has to be told about them before
     * they exist, because it checks that a texture is there and the pictures are made
     * in the same run as the models that name them.
     */
    public static List<String> names() {
        List<String> names = new ArrayList<>();
        for (Generator generator : Generator.all()) {
            names.add(generator(generator, false));
            names.add(generator(generator, true));
        }
        for (Tier tier : Tier.values()) {
            names.add(battery(tier));
        }
        return names;
    }

    public static int[][] generatorSkin(boolean lit) {
        int[][] pixels = plate();
        for (int y = WINDOW_FROM; y < WINDOW_TO; y++) {
            for (int x = WINDOW_FROM; x < WINDOW_TO; x++) {
                // A fire is brightest at its base, so the mouth is graded rather than
                // filled: a flat orange square reads as a sticker on the front.
                int depth = y - WINDOW_FROM;
                int colour = lit
                        ? mix(EMBER, MOUTH_LIT, depth / (float) (WINDOW_TO - WINDOW_FROM - 1))
                        : MOUTH_COLD;
                pixels[y][x] = 0xFF000000 | colour;
            }
        }
        return pixels;
    }

    public static int[][] batterySkin(Tier tier) {
        int colour = CELL[Math.min(tier.ordinal(), CELL.length - 1)];
        int[][] pixels = plate();
        for (int y = WINDOW_FROM; y < WINDOW_TO; y++) {
            for (int x = WINDOW_FROM; x < WINDOW_TO; x++) {
                // Cells stood side by side: every third column is the gap between two.
                boolean gap = (x - WINDOW_FROM) % 3 == 2;
                pixels[y][x] = 0xFF000000 | (gap ? EDGE : colour);
            }
        }
        return pixels;
    }

    /** Metal with a darker rim and a rivet in each corner. */
    private static int[][] plate() {
        int[][] pixels = new int[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean rim = x == 0 || y == 0 || x == SIZE - 1 || y == SIZE - 1;
                boolean rivet = (x == 2 || x == SIZE - 3) && (y == 2 || y == SIZE - 3);
                int colour = rim ? EDGE : rivet ? RIVET : grain(BODY, x, y);
                pixels[y][x] = 0xFF000000 | colour;
            }
        }
        return pixels;
    }

    /**
     * A little unevenness so a flat colour does not read as plastic. Deterministic on
     * the position: the same picture has to come out of every run, or datagen would
     * report a change every time it was asked.
     */
    private static int grain(int colour, int x, int y) {
        int shift = (((x * 7 + y * 13) % 5) - 2) * 4;
        return channel(colour, 16, shift) | channel(colour, 8, shift) | channel(colour, 0, shift);
    }

    private static int channel(int colour, int at, int shift) {
        int value = Math.clamp(((colour >> at) & 0xFF) + shift, 0, 0xFF);
        return value << at;
    }

    private static int mix(int from, int to, float amount) {
        int red = blend(from, to, 16, amount);
        int green = blend(from, to, 8, amount);
        int blue = blend(from, to, 0, amount);
        return red | green | blue;
    }

    private static int blend(int from, int to, int at, float amount) {
        int start = (from >> at) & 0xFF;
        int end = (to >> at) & 0xFF;
        return Math.round(start + (end - start) * amount) << at;
    }
}
