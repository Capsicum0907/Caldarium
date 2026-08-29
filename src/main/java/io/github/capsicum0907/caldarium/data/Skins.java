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

    // ---- the screen -------------------------------------------------------
    //
    // Where everything on the panel sits. ⚠ These are read twice — once here to
    // paint the picture and once by the screen to draw on top of it — so they are
    // constants rather than numbers written into either side. A slot the painter
    // and the screen disagree about is a slot drawn in mid-air.

    public static final int SHEET = 256;
    public static final int GUI_WIDTH = 176;
    public static final int GUI_HEIGHT = 166;

    public static final int FUEL_SLOT_X = 80;
    public static final int FUEL_SLOT_Y = 35;

    public static final int FLAME_X = 80;
    public static final int FLAME_Y = 53;
    public static final int FLAME_W = 14;
    public static final int FLAME_H = 14;
    public static final int FLAME_U = 176;
    public static final int FLAME_V = 0;

    public static final int BAR_X = 152;
    public static final int BAR_Y = 17;
    public static final int BAR_W = 12;
    public static final int BAR_H = 52;
    public static final int BAR_U = 176;
    public static final int BAR_V = 16;

    // ⚠ The slot and the recess under it are NOT painted into the panel. A battery
    // has no fuel, and a panel carrying them showed it an empty slot it would not
    // accept anything into and a hollow that never lit. They are strips like the
    // others, drawn only by a screen whose machine burns.
    public static final int FUEL_WELL_U = 176;
    public static final int FUEL_WELL_V = 72;
    public static final int FLAME_WELL_U = 176;
    public static final int FLAME_WELL_V = 92;

    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_Y = 84;
    public static final int HOTBAR_Y = 142;
    public static final int SLOT = 18;

    public static final String GUI = "machine";

    private static final int PANEL = 0xC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFF;
    private static final int PANEL_DARK = 0x555555;
    private static final int WELL = 0x8B8B8B;
    private static final int WELL_DARK = 0x373737;
    private static final int CHARGE = 0xE8C33A;

    /** The panel, and beside it the strips drawn over it as things fill up. */
    public static int[][] gui() {
        int[][] pixels = new int[SHEET][SHEET];
        panel(pixels);
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                well(pixels, INVENTORY_X + x * SLOT, INVENTORY_Y + y * SLOT, SLOT, SLOT);
            }
            well(pixels, INVENTORY_X + x * SLOT, HOTBAR_Y, SLOT, SLOT);
        }
        well(pixels, BAR_X - 1, BAR_Y - 1, BAR_W + 2, BAR_H + 2);

        // The two the burner adds to the panel it shares with the battery.
        well(pixels, FUEL_WELL_U, FUEL_WELL_V, SLOT, SLOT);
        well(pixels, FLAME_WELL_U, FLAME_WELL_V, FLAME_W, FLAME_H);

        // The two strips. Both are drawn from the bottom up, so a partly filled bar
        // is the bottom of the strip rather than a scaled copy of the whole of it.
        flame(pixels, FLAME_U, FLAME_V);
        for (int y = 0; y < BAR_H; y++) {
            for (int x = 0; x < BAR_W; x++) {
                // Brighter towards the top, so a full bar does not read as flat paint.
                pixels[BAR_V + y][BAR_U + x] = 0xFF000000 | mix(EMBER, CHARGE, 1.0F - y / (float) (BAR_H - 1));
            }
        }
        return pixels;
    }

    private static void panel(int[][] pixels) {
        fill(pixels, 0, 0, GUI_WIDTH, GUI_HEIGHT, PANEL);
        for (int x = 0; x < GUI_WIDTH; x++) {
            pixels[0][x] = 0xFF000000 | PANEL_LIGHT;
            pixels[GUI_HEIGHT - 1][x] = 0xFF000000 | PANEL_DARK;
        }
        for (int y = 0; y < GUI_HEIGHT; y++) {
            pixels[y][0] = 0xFF000000 | PANEL_LIGHT;
            pixels[y][GUI_WIDTH - 1] = 0xFF000000 | PANEL_DARK;
        }
    }

    /** A recess: dark along the top and left, light along the bottom and right. */
    private static void well(int[][] pixels, int left, int top, int width, int height) {
        fill(pixels, left, top, width, height, WELL);
        for (int x = 0; x < width; x++) {
            pixels[top][left + x] = 0xFF000000 | WELL_DARK;
            pixels[top + height - 1][left + x] = 0xFF000000 | PANEL_LIGHT;
        }
        for (int y = 0; y < height; y++) {
            pixels[top + y][left] = 0xFF000000 | WELL_DARK;
            pixels[top + y][left + width - 1] = 0xFF000000 | PANEL_LIGHT;
        }
    }

    /** A flame: widest at the base, tapering, drawn from the same two fire colours. */
    private static void flame(int[][] pixels, int atX, int atY) {
        for (int y = 0; y < FLAME_H; y++) {
            float up = y / (float) (FLAME_H - 1);
            int half = Math.round((1.0F - up) * (FLAME_W / 2.0F - 1.0F)) + 1;
            for (int x = 0; x < FLAME_W; x++) {
                int from = Math.abs(x - (FLAME_W - 1) / 2);
                if (from > half) {
                    continue;
                }
                pixels[atY + FLAME_H - 1 - y][atX + x] =
                        0xFF000000 | mix(MOUTH_LIT, EMBER, up);
            }
        }
    }

    private static void fill(int[][] pixels, int left, int top, int width, int height, int colour) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[top + y][left + x] = 0xFF000000 | colour;
            }
        }
    }
}
