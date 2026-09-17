package io.github.capsicum0907.caldarium.data;

import java.util.ArrayList;
import java.util.List;

import io.github.capsicum0907.caldarium.Generator;
import io.github.capsicum0907.caldarium.Kind;
import io.github.capsicum0907.caldarium.Source;
import io.github.capsicum0907.caldarium.Tier;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

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
     * <p>⚠ Four goes at this. A lattice, a lattice with a seam, a seam with a
     * reflection, then no structure at all - and the last was wrong too. The
     * reference has one thing on it and it is a bright line every third column, seen
     * by taking the column means rather than by looking: a table of colours hides
     * how they are arranged, and looking at one is how three of the four attempts
     * went wrong.
     */
    private static final int CELL_RESTING = 0x24405E;
    private static final int CELL_WORKING = 0x3A8CD4;

    /**
     * ⚠ Lines, not grain. Measured off the reference rather than guessed at: the
     * column means of its face run 137 112 111 136 112 110 ... and the row means are
     * flat, which is a bright line every third column and nothing else going on
     * across. It is the collector on a photovoltaic cell, and it is what makes the
     * surface read as one.
     */
    private static final int LINE_EVERY = 3;
    private static final int LINE_LIFT = 26;

    /** How far a pixel may stray from its colour. Small, so the lines stay lines. */
    private static final int SPECKLE = 8;


    private static final int WINDOW_FROM = 4;
    private static final int WINDOW_TO = 12;

    private Skins() {
    }

    /** How tall a panel is, in pixels. Read by the block, the model and the picture. */
    public static final int PANEL_HEIGHT = 3;

    // ---- what a thing that carries is shaped like ------------------------
    //
    // ⚠ Read twice, like everything else in this file: once by the model that draws
    // the block and once by the shape you bump into. A number written into one of them
    // is a block you can walk through the visible half of.

    /**
     * How wide an arm is: the one number the whole of this shape is built out of.
     * Everything else here follows it, so a cable is made thinner or fatter by
     * changing this and nothing else.
     *
     * <p>⚠ Even numbers only. An odd width cannot sit in the middle of sixteen
     * pixels, and a cable half a pixel off centre meets the next one with a step in it.
     */
    public static final int ARM_ACROSS = 2;

    /**
     * ⭐ <b>A cable's middle is exactly as thick as its arms.</b> It was wider, and a
     * straight run of it came out lumpy — wide, narrow, wide, narrow — because every
     * block showed its middle between two thinner arms. Matched, a run is one smooth
     * square tube and the joins are invisible, which is what a length of cable is.
     */
    public static final int CORE_CABLE = ARM_ACROSS;

    /**
     * ⭐ A door's middle is the wire's own thickness, so a run through one is as smooth
     * as a run without. ⚠ It was wider twice over — ten, then six — and both times it
     * put a bulge in the line either side of the drill, which is the same lumpiness
     * that made a plain run of cable look like a string of beads.
     *
     * <p>Nothing is lost by it. The middle used to carry the arrow that told an
     * importer from an exporter; the drill tells them apart now, from any side and at
     * any distance, which six pixels of arrow never did.
     */
    public static final int CORE_DOOR = CORE_CABLE;

    /**
     * ⚠ Exactly as deep as the gap between the face of the block and the middle, so
     * an arm meets a cable's middle edge to edge rather than overlapping it. Two
     * surfaces in the same plane that <em>overlap</em> flicker against each other at
     * every distance; two that merely touch along an edge do not. ⭐ The face where an
     * arm meets the middle is left off the model instead, which is the other half of
     * keeping the two out of each other's way.
     */
    public static final int ARM_DEEP = (SIZE - CORE_CABLE) / 2;

    /**
     * A drill: square steps widening towards the face of the block. It is what says a
     * door reaches <em>out</em> of the line on that side rather than along it, which is
     * the one thing about a door that was not visible from anywhere.
     */
    public static final int DRILL_STEPS = 3;

    /**
     * The flange, pressed against whatever the door is working on.
     *
     * <p>⚠ Even, and evenly reachable: the steps are spaced from {@link #ARM_ACROSS} up
     * to here, and every one of them has to come out even or it cannot sit in the middle
     * of sixteen pixels. From two, that leaves six, ten and fourteen.
     */
    public static final int DRILL_ACROSS_MOST = 6;

    /**
     * How wide one step is, counting out from the middle: evenly spaced from the
     * wire's own thickness up to the flange, so the cone stays a cone when the wire
     * is made thinner.
     */
    public static int drillAcross(int step) {
        return ARM_ACROSS + (DRILL_ACROSS_MOST - ARM_ACROSS) * step / (DRILL_STEPS - 1);
    }

    /**
     * Where one step starts and stops along the arm.
     *
     * <p>⚠ Divided rather than multiplied by a fixed depth. The steps have to fill an
     * arm exactly: a step short of it leaves a gap between the drill and the middle,
     * and an arm whose depth does not divide by three is what a thinner wire gives.
     */
    /**
     * Where one step starts and stops along the arm.
     *
     * <p>⚠ Divided rather than multiplied by a fixed depth. The steps have to fill an
     * arm exactly: a step short of it leaves a gap between the drill and the middle,
     * and an arm whose depth does not divide by three is what a thinner wire gives.
     *
     * <p>⭐ Read by the picture as well as by the shape. The side of a step shows the
     * rows of the texture between the same two numbers, so a ring drawn here is a band
     * on that step and the two cannot fall out of step with each other.
     */
    public static int drillAt(int step) {
        return step * ARM_DEEP / DRILL_STEPS;
    }

    /**
     * One box, as {@code from} and {@code to}, pointed at a side of the block.
     *
     * <p>⭐ <b>Here and nowhere else.</b> The shape you bump into and the model you look
     * at are built by two different classes, and a block whose model is drawn where
     * nothing can be bumped into is a block you can walk through the visible half of.
     * They read this instead of each keeping a copy of the arithmetic.
     */
    public static int[] box(Direction side, int across, int from, int to) {
        int near = (SIZE - across) / 2;
        int far = near + across;
        int back = SIZE - from;
        int front = SIZE - to;
        return switch (side) {
            case NORTH -> new int[] { near, near, from, far, far, to };
            case SOUTH -> new int[] { near, near, front, far, far, back };
            case WEST -> new int[] { from, near, near, to, far, far };
            case EAST -> new int[] { front, near, near, back, far, far };
            case DOWN -> new int[] { near, from, near, far, to, far };
            case UP -> new int[] { near, front, near, far, back, far };
        };
    }

    /** The plain post a face on to the line wears. */
    public static int[] armBox(Direction side) {
        return box(side, ARM_ACROSS, 0, ARM_DEEP);
    }

    /**
     * One step of a drill, counting out from the middle.
     *
     * <p>⭐ The same three steps either way round, and which way round says what the
     * block does: a <b>mouth</b> puts its widest step against what it takes from, and a
     * <b>nozzle</b> puts its narrowest against what it gives to. So the direction the
     * shape narrows in is the direction the energy goes, which is readable from any
     * angle and at any distance — unlike an arrow on a six-pixel face.
     */
    public static int[] drillBox(Direction side, int step, boolean mouth) {
        int out = mouth ? DRILL_STEPS - 1 - step : step;
        return box(side, drillAcross(step), drillAt(out), drillAt(out + 1));
    }

    /** The middle of anything that carries, whatever kind it is. */
    public static int[] middleBox(int core) {
        int in = (SIZE - core) / 2;
        return new int[] { in, in, in, in + core, in + core, in + core };
    }

    /** The metal a length of wire is made of, in the colour of its rung. */
    public static String arm(Tier tier) {
        return tier.id() + "_arm";
    }

    /** The metal a drill is made of. Banded, where an arm is not. */
    public static String drill(Tier tier) {
        return tier.id() + "_drill";
    }

    /** How much brighter the lit side of a wire is than the shaded one. */
    private static final int ROUND_LIT = 20;

    /** How much brighter the outermost band of a drill is, and how dark a joint. */
    private static final int EDGE_LIT = 14;
    private static final int SCORE = 26;

    /**
     * A length of wire.
     *
     * <p>⭐ <b>Drawn for where it is read, which is two rows and two columns.</b> No UV
     * is named on the models, so the game cuts the picture out of the shape: the four
     * long sides of a two-pixel post show the middle two columns of this, and the
     * middle two rows, and nothing else — not the edge, not a corner, not the middle of
     * the face. A picture drawn for a whole block face is a picture this size never
     * shows, which is what the plate was, and why a run of cable came out as a smear.
     *
     * <p>So the shading is put exactly there: one side of the span light and the other
     * dark, which is what makes a square post read as a round wire from any side.
     */
    public static int[][] armSkin(Tier tier) {
        int body = mix(BODY, tier.colour(), EDGE_TINT);
        int[][] pixels = new int[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                pixels[y][x] = 0xFF000000 | shift(grain(body, x, y), round(x) + round(y));
            }
        }
        return pixels;
    }

    /** Light on the near edge of the wire's own span, dark on the far one, flat outside. */
    private static int round(int at) {
        int near = (SIZE - ARM_ACROSS) / 2;
        if (at < near || at >= near + ARM_ACROSS) {
            return 0;
        }
        return ROUND_LIT - 2 * ROUND_LIT * (at - near) / Math.max(1, ARM_ACROSS - 1);
    }

    /**
     * A drill.
     *
     * <p>⭐ <b>Square rings, because square rings are what a drill shows.</b> Each step
     * is a box a little wider than the last, and the side of a box that wide shows the
     * rows of the picture at exactly its own depth — so the ring at {@link #drillAt} is
     * a band on the step that meets there. The steps are scored apart by that, and the
     * outermost ring is lifted, which is the edge you see against whatever it works on.
     *
     * <p>Four-fold symmetric on purpose: every step has four sides and they have to be
     * the same picture, which a gradient across the square would not give.
     */
    public static int[][] drillSkin(Tier tier) {
        int body = mix(BODY, tier.colour(), EDGE_TINT);
        int[][] pixels = new int[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int ring = Math.min(Math.min(x, y), Math.min(SIZE - 1 - x, SIZE - 1 - y));
                pixels[y][x] = 0xFF000000 | shift(grain(body, x, y), band(ring));
            }
        }
        return pixels;
    }

    private static int band(int ring) {
        for (int step = 1; step < DRILL_STEPS; step++) {
            if (ring == drillAt(step)) {
                return -SCORE;
            }
        }
        return ring < drillAt(1) ? EDGE_LIT : 0;
    }

    /** The name of the texture for a generator, resting or working. */
    public static String generator(Generator.Made made, boolean lit) {
        return lit ? made.id() + "_on" : made.id();
    }

    /** The face a panel points at the sky. */
    public static String generatorTop(Generator.Made made, boolean lit) {
        return lit ? made.id() + "_top_on" : made.id() + "_top";
    }

    /** Its edge and its underside, which are the same plain metal. */
    public static String generatorSide(Generator.Made made) {
        return made.id() + "_side";
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
        names.add(SOL);
        names.add(SOL_LIT);
        for (Generator.Made made : Generator.made()) {
            if (made.source().flat()) {
                names.add(generatorTop(made, false));
                names.add(generatorTop(made, true));
                names.add(generatorSide(made));
            } else {
                names.add(generator(made, false));
                names.add(generator(made, true));
            }
        }
        for (Kind kind : Kind.values()) {
            if (kind.carries()) {
                continue;
            }
            for (Tier tier : Tier.upTo(kind.top())) {
                names.add(kind(kind, tier));
            }
        }
        for (Tier tier : Tier.upTo(Kind.highestCarried())) {
            names.add(arm(tier));
            names.add(drill(tier));
        }
        return names;
    }

    /**
     * The same plate for every generator that is a box, and the window says what it
     * draws on: a mouth to feed, or a pool to fill. What is flat has its own face.
     */
    public static int[][] generatorSkin(Generator.Made made, boolean lit) {
        int[][] pixels = plate();
        int span = WINDOW_TO - WINDOW_FROM - 1;
        for (int y = WINDOW_FROM; y < WINDOW_TO; y++) {
            for (int x = WINDOW_FROM; x < WINDOW_TO; x++) {
                float down = (y - WINDOW_FROM) / (float) span;
                // A fire is brightest at its base, so the mouth is graded rather than
                // filled: a flat orange square reads as a sticker on the front. A pool
                // is the other way up, because its light is at the surface.
                pixels[y][x] = 0xFF000000 | (made.source() == Source.ITEM
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
        int colour = tier.colour();
        int[][] pixels = plate();
        for (int y = WINDOW_FROM; y < WINDOW_TO; y++) {
            for (int x = WINDOW_FROM; x < WINDOW_TO; x++) {
                pixels[y][x] = 0xFF000000 | (marked(kind, x, y) ? colour : EDGE);
            }
        }
        return pixels;
    }

    /**
     * What the window shows: cells stood side by side, a socket to plug into, a
     * junction, or an arrow saying which way across the boundary it works.
     *
     * <p>⭐ An exhaustive switch, so a kind added to the table is a compiler error
     * here rather than a block wearing another one's face.
     *
     * <p>The three that carry are told apart by that arrow alone, and it is drawn as
     * large as the window will hold: an importer and an exporter placed next to each
     * other have to be legible from across the room, which is the only place anybody
     * ever looks at a line of cable from.
     */
    private static boolean marked(Kind kind, int x, int y) {
        int across = x - WINDOW_FROM;
        int down = y - WINDOW_FROM;
        int span = WINDOW_TO - WINDOW_FROM;
        return switch (kind) {
            // Every third column is the gap between two cells.
            case BATTERY -> across % 3 != 2;
            // A ring one pixel in, and a contact in the middle of it.
            case CHARGER -> {
                int in = Math.min(Math.min(across, down),
                        Math.min(span - 1 - across, span - 1 - down));
                yield in == 1 || in == 3;
            }
            // ⭐ Nothing at all. These have no face to put a window in: two pixels of
            // wire shows two pixels of picture, and what they are is said by the shape
            // instead — a drill that widens towards what it draws from, or narrows
            // towards what it feeds. An arrow here was read by nobody.
            case CABLE, IMPORTER, EXPORTER -> false;
        };
    }

    /**
     * The face of a panel: cells behind glass in a frame.
     *
     * <p>Four rows of four, so the grid reads at a glance and still fits the frame,
     * and each cell is lighter towards its top left - a flat blue square looks
     * painted on, while a sheen looks like something under glass.
     */
    public static int[][] solarSkin(Tier tier, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        // ⭐ The glass is the same glass on every rung; what the rung changes is the
        // cast of it. Mixing towards the metal keeps one picture for all of them and
        // still tells them apart from above, which is the only side anybody sees.
        int base = mix(lit ? CELL_WORKING : CELL_RESTING, tier.colour(), TIER_TINT);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int colour = x % LINE_EVERY == 0 ? shift(base, LINE_LIFT) : base;
                pixels[y][x] = 0xFF000000 | speckle(colour, x, y);
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
        return shift(colour, Math.floorMod(hash, 2 * SPECKLE + 1) - SPECKLE);
    }

    /** The same colour, brighter or darker, with its hue left where it was. */
    private static int shift(int colour, int by) {
        return channel(colour, 16, by) | channel(colour, 8, by) | channel(colour, 0, by);
    }

    /** The edge and underside of a panel: metal, in the colour of its rung. */
    public static final String SOL = "sol";

    /**
     * A single white pixel for the sphere to hang its vertex colours on.
     *
     * <p>The ball is not textured: {@code Sunspots} answers what colour a point of it
     * is from where that point is, so there is nothing to wrap and nothing to stretch.
     * {@link #SOL} is still drawn, for the block in a hand and in the list.
     */
    public static final String SOL_LIT = "sol_lit";

    public static int[][] litSkin() {
        return new int[][] { { 0xFFFFFFFF, 0xFFFFFFFF }, { 0xFFFFFFFF, 0xFFFFFFFF } };
    }

    /** The size of the sun's own picture, which is wrapped round a sphere. */
    private static final int SOL_SIZE = 32;

    /**
     * Cells of molten stuff, hot in the middle of each and dark at the seams.
     *
     * <p>Wraps in both directions, because it goes round a ball: the noise is sampled
     * on a torus rather than on a square, so there is no line down the back of it.
     */
    public static int[][] solSkin(float phase) {
        int[][] pixels = new int[SOL_SIZE][SOL_SIZE];
        for (int y = 0; y < SOL_SIZE; y++) {
            for (int x = 0; x < SOL_SIZE; x++) {
                float heat = cells(x, y, 4, phase) * 0.6F + cells(x, y, 8, phase * 1.6F) * 0.3F
                        + cells(x, y, 16, phase * 2.4F) * 0.1F;
                // ⚠ cells() is the distance to a cell's middle, so the middle comes
                // back as nought. A sun is brightest in the middle of each blob and
                // dark where they meet, which is this the other way up.
                heat = Math.clamp(1.0F - (heat - 0.15F) * 1.5F, 0.0F, 1.0F);
                pixels[y][x] = 0xFF000000 | (heat < 0.5F
                        ? mix(0xC2400C, 0xF9A11B, heat * 2.0F)
                        : mix(0xF9A11B, 0xFFF6D8, (heat - 0.5F) * 2.0F));
            }
        }
        return pixels;
    }

    /** Distance to the nearest of a lattice of drifting points, wrapped both ways. */
    private static float cells(int x, int y, int across, float phase) {
        float step = (float) SOL_SIZE / across;
        float best = Float.MAX_VALUE;
        int cx = (int) (x / step);
        int cy = (int) (y / step);
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int gx = Math.floorMod(cx + dx, across);
                int gy = Math.floorMod(cy + dy, across);
                float drift = Mth.TWO_PI * phase;
                float px = (gx + jitter(gx, gy, 1)
                        + 0.30F * Mth.sin(drift + jitter(gx, gy, 3) * Mth.TWO_PI)) * step;
                float py = (gy + jitter(gx, gy, 2)
                        + 0.30F * Mth.cos(drift + jitter(gx, gy, 4) * Mth.TWO_PI)) * step;
                float ox = wrapped(x - px);
                float oy = wrapped(y - py);
                best = Math.min(best, ox * ox + oy * oy);
            }
        }
        return Math.clamp((float) Math.sqrt(best) / step, 0.0F, 1.0F);
    }

    private static float wrapped(float d) {
        float half = SOL_SIZE / 2.0F;
        if (d > half) {
            return d - SOL_SIZE;
        }
        return d < -half ? d + SOL_SIZE : d;
    }

    private static float jitter(int x, int y, int salt) {
        int h = x * 374761393 + y * 668265263 + salt * 1274126177;
        h = (h ^ (h >> 13)) * 1274126177;
        return ((h ^ (h >> 16)) & 0xFFFF) / 65535.0F;
    }

    public static int[][] plainSkin(Tier tier) {
        return plate(mix(BODY, tier.colour(), EDGE_TINT));
    }


    /** How far the pictures are pulled towards the metal of the rung they are on. */
    private static final float TIER_TINT = 0.34F;
    private static final float EDGE_TINT = 0.30F;

    private static int[][] plate() {
        return plate(BODY);
    }

    /** Metal with a darker rim and a rivet in each corner. */
    private static int[][] plate(int body) {
        int[][] pixels = new int[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean rim = x == 0 || y == 0 || x == SIZE - 1 || y == SIZE - 1;
                boolean rivet = (x == 2 || x == SIZE - 3) && (y == 2 || y == SIZE - 3);
                int colour = rim ? EDGE : rivet ? RIVET : grain(body, x, y);
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
    public static final int POUR_W = 44;
    public static final int POUR_H = 16;
    public static final int POUR_Y = 44;
    public static final int POUR_GAP = 2;

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
