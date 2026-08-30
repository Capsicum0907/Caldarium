package io.github.capsicum0907.caldarium.data;

import java.util.ArrayList;
import java.util.List;

import io.github.capsicum0907.caldarium.Generator;
import io.github.capsicum0907.caldarium.Kind;
import io.github.capsicum0907.caldarium.Source;
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

    private static final int MOLTEN_COLD = 0x3A3A40;

    /**
     * A cell is nearly black when it is resting and lights up when it is working.
     *
     * <p>⚠ The first attempt made them pale blue, which read as a tiled floor rather
     * than as anything under glass. What sells a panel is the contrast: dark cells
     * held apart by bright metal, not light cells with dark gaps between them.
     */
    /**
     * The face is a colour with grain on it and nothing else.
     *
     * <p>⚠ Three goes at this drew a lattice, then a lattice with a seam, then a
     * seam with a reflection, and each one looked more like a tiled floor than the
     * one before. The mods this was measured against have no lattice at all: their
     * faces are a single colour, pixel by pixel a little lighter or darker, and what
     * makes them read as panels is the shape they are on and the colour they are.
     * The structure was the mistake, three times over.
     */
    private static final int CELL_RESTING = 0x24405E;
    private static final int CELL_WORKING = 0x3A8CD4;

    /** How far a pixel of the face may stray from that colour, either way. */
    private static final int SPECKLE = 16;

    /**
     * One per rung of the ladder, taken by ordinal; the last one repeats if it runs
     * out. The metal each tier is made of, so a block says which rung it is on
     * without anything written on it.
     */
    // ⚠ The netherite one is the ingot's highlight rather than the block's dark
    // face. Netherite is the darkest metal there is, and drawn true it came out at
    // 0x5B4E52 against a 0x51565A window — a top tier that looked like a blank plate.
    private static final int[] CELL = { 0xD5DBE0, 0xF0C246, 0x5BE0D6, 0xB0A2A5 };

    private static final int WINDOW_FROM = 4;
    private static final int WINDOW_TO = 12;

    private Skins() {
    }

    /** How tall a panel is, in pixels. Read by the block, the model and the picture. */
    public static final int PANEL_HEIGHT = 3;

    /** The name of the texture for a generator, resting or working. */
    public static String generator(Generator generator, boolean lit) {
        return lit ? generator.id() + "_on" : generator.id();
    }

    /** The face a panel points at the sky. */
    public static String generatorTop(Generator generator, boolean lit) {
        return lit ? generator.id() + "_top_on" : generator.id() + "_top";
    }

    /** Its edge and its underside, which are the same plain metal. */
    public static String generatorSide(Generator generator) {
        return generator.id() + "_side";
    }

    public static String kind(Kind kind, Tier tier) {
        return kind.id(tier);
    }

    /**
     * The names this will write. The model provider has to be told about them before
     * they exist, because it checks that a texture is there and the pictures are made
     * in the same run as the models that name them.
     */
    public static List<String> names() {
        List<String> names = new ArrayList<>();
        for (Generator generator : Generator.all()) {
            if (generator.source().flat()) {
                names.add(generatorTop(generator, false));
                names.add(generatorTop(generator, true));
                names.add(generatorSide(generator));
            } else {
                names.add(generator(generator, false));
                names.add(generator(generator, true));
            }
        }
        for (Kind kind : Kind.values()) {
            for (Tier tier : Tier.values()) {
                names.add(kind(kind, tier));
            }
        }
        return names;
    }

    /**
     * The same plate for every generator that is a box, and the window says what it
     * draws on: a mouth to feed, or a pool to fill. What is flat has its own face.
     */
    public static int[][] generatorSkin(Generator generator, boolean lit) {
        int[][] pixels = plate();
        int span = WINDOW_TO - WINDOW_FROM - 1;
        for (int y = WINDOW_FROM; y < WINDOW_TO; y++) {
            for (int x = WINDOW_FROM; x < WINDOW_TO; x++) {
                float down = (y - WINDOW_FROM) / (float) span;
                // A fire is brightest at its base, so the mouth is graded rather than
                // filled: a flat orange square reads as a sticker on the front. A pool
                // is the other way up, because its light is at the surface.
                pixels[y][x] = 0xFF000000 | (generator.source() == Source.ITEM
                        ? (lit ? mix(EMBER, MOUTH_LIT, down) : MOUTH_COLD)
                        : (lit ? mix(MOUTH_LIT, EMBER, down) : MOLTEN_COLD));
            }
        }
        return pixels;
    }

    /**
     * The same plate for every kind; only what is in the window differs. They are one
     * machine seen doing different jobs, and the picture should say so.
     */
    public static int[][] kindSkin(Kind kind, Tier tier) {
        int colour = CELL[Math.min(tier.ordinal(), CELL.length - 1)];
        int[][] pixels = plate();
        for (int y = WINDOW_FROM; y < WINDOW_TO; y++) {
            for (int x = WINDOW_FROM; x < WINDOW_TO; x++) {
                pixels[y][x] = 0xFF000000 | (marked(kind, x, y) ? colour : EDGE);
            }
        }
        return pixels;
    }

    /** What the window shows: cells stood side by side, or a socket to plug into. */
    private static boolean marked(Kind kind, int x, int y) {
        return switch (kind) {
            // Every third column is the gap between two cells.
            case BATTERY -> (x - WINDOW_FROM) % 3 != 2;
            // A ring one pixel in, and a contact in the middle of it.
            case CHARGER -> {
                int in = Math.min(Math.min(x - WINDOW_FROM, y - WINDOW_FROM),
                        Math.min(WINDOW_TO - 1 - x, WINDOW_TO - 1 - y));
                yield in == 1 || in == 3;
            }
        };
    }

    /**
     * The face of a panel: cells behind glass in a frame.
     *
     * <p>Four rows of four, so the grid reads at a glance and still fits the frame,
     * and each cell is lighter towards its top left - a flat blue square looks
     * painted on, while a sheen looks like something under glass.
     */
    public static int[][] solarSkin(boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        int base = lit ? CELL_WORKING : CELL_RESTING;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                pixels[y][x] = 0xFF000000 | speckle(base, x, y);
            }
        }
        return pixels;
    }

    /**
     * One pixel of the face, its brightness knocked about a little.
     *
     * <p>Deterministic on the position, because datagen has to write the same file
     * every time it runs or it reports a change on every build.
     *
     * <p>The shift is the same on all three channels, so what varies is how bright a
     * pixel is and not what colour it is: a hue that wanders turns a flat surface
     * into confetti.
     */
    private static int speckle(int colour, int x, int y) {
        int hash = x * 73856093 ^ y * 19349663;
        hash ^= hash >>> 13;
        int shift = Math.floorMod(hash, 2 * SPECKLE + 1) - SPECKLE;
        return channel(colour, 16, shift) | channel(colour, 8, shift) | channel(colour, 0, shift);
    }

    /** The edge and underside of a panel: metal, and nothing else. */
    public static int[][] plainSkin() {
        return plate();
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

    public static final int SLOT = 18;
    public static final int SLOT_ROW_Y = 35;

    /**
     * Where the flame goes. ⚠ There is no flame <em>in</em> this sheet: the screen
     * borrows the one the furnace uses, which is why the size is vanilla's fourteen
     * rather than a number of ours. Drawn here it came out a tapering orange block
     * that read as a traffic cone, and the recess around it never lined up with it.
     */
    public static final int FLAME_X = GUI_WIDTH / 2 - 7;
    public static final int FLAME_Y = 53;
    public static final int FLAME_W = 14;
    public static final int FLAME_H = 14;

    public static final int BAR_X = 152;
    public static final int BAR_Y = 17;
    public static final int BAR_W = 12;
    public static final int BAR_H = 52;
    public static final int BAR_U = 176;
    public static final int BAR_V = 16;

    // The tank, opposite the charge. A machine with no tank draws neither.
    public static final int TANK_X = 12;
    public static final int TANK_Y = 17;
    public static final int TANK_U = 176;
    public static final int TANK_V = 112;
    public static final int TANK_WELL_U = 192;
    public static final int TANK_WELL_V = 0;
    public static final int TANK_WELL_W = BAR_W + 2;
    public static final int TANK_WELL_H = BAR_H + 2;

    // ⚠ The slot and the recess under it are NOT painted into the panel. A battery
    // has no fuel, and a panel carrying them showed it an empty slot it would not
    // accept anything into and a hollow that never lit. They are strips like the
    // others, drawn only by a screen whose machine burns.
    public static final int FUEL_WELL_U = 176;
    public static final int FUEL_WELL_V = 72;

    public static final int INVENTORY_X = 8;
    public static final int INVENTORY_Y = 84;
    public static final int HOTBAR_Y = 142;

    /**
     * How wide a row of a machine's own slots may be.
     *
     * <p>⚠ Five, not nine. A row of nine reaches x=169, and the charge bar starts at
     * x=151: the top tier of charger would have laid its last two slots underneath
     * it. Wrapping is what keeps the layout a rule rather than a rule with an
     * exception at the far end of the ladder.
     */
    public static final int SLOT_COLUMNS = 5;

    public static int slotColumns(int count) {
        return Math.min(count, SLOT_COLUMNS);
    }

    public static int slotRows(int count) {
        return (count + SLOT_COLUMNS - 1) / SLOT_COLUMNS;
    }

    /**
     * Where one of a machine's own slots goes: a grid of however many there are,
     * centred, and every row centred within it so a short last row does not sit off
     * to one side. One rule for a burner's single slot of fuel and for a charger's
     * nine, so a kind added later has a place to put its slots without a layout of
     * its own.
     */
    public static int slotX(int index, int count) {
        int columns = slotColumns(count);
        int row = index / columns;
        int wide = Math.min(columns, count - row * columns);
        return GUI_WIDTH / 2 - wide * SLOT / 2 + index % columns * SLOT + 1;
    }

    public static int slotY(int index, int count) {
        int row = index / slotColumns(count);
        return SLOT_ROW_Y - (slotRows(count) - 1) * SLOT / 2 + row * SLOT;
    }

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

        // ⚠ The tank recess is NOT painted into the panel, for the same reason the
        // fuel slot is not: the panel is shared, and a battery drawn with one was
        // showing a hollow that nothing could ever go into. Made twice now.
        well(pixels, TANK_WELL_U, TANK_WELL_V, TANK_WELL_W, TANK_WELL_H);

        // The two the burner adds to the panel it shares with the battery.
        well(pixels, FUEL_WELL_U, FUEL_WELL_V, SLOT, SLOT);

        // The two strips. Both are drawn from the bottom up, so a partly filled bar
        // is the bottom of the strip rather than a scaled copy of the whole of it.
        for (int y = 0; y < BAR_H; y++) {
            for (int x = 0; x < BAR_W; x++) {
                // Brighter towards the top, so a full bar does not read as flat paint.
                pixels[BAR_V + y][BAR_U + x] =
                        0xFF000000 | mix(EMBER, CHARGE, 1.0F - y / (float) (BAR_H - 1));
                // The tank is the same shape in the colour of what is in it.
                pixels[TANK_V + y][TANK_U + x] =
                        0xFF000000 | mix(EMBER, MOUTH_LIT, 1.0F - y / (float) (BAR_H - 1));
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

    private static void fill(int[][] pixels, int left, int top, int width, int height, int colour) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[top + y][left + x] = 0xFF000000 | colour;
            }
        }
    }
}
