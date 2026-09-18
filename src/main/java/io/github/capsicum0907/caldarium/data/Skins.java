package io.github.capsicum0907.caldarium.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.capsicum0907.caldarium.SolPalette;
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
 */
public final class Skins {
    public static final int SIZE = 16;


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
    private static final int LAMP_CELL_RESTING = 0x4A3418;
    private static final int LAMP_CELL_WORKING = 0xE0A040;

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
        int[][] pixels = new int[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                pixels[y][x] = 0xFF000000 | shift(metal(tier, x, y), round(x) + round(y));
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
        int[][] pixels = new int[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int ring = Math.min(Math.min(x, y), Math.min(SIZE - 1 - x, SIZE - 1 - y));
                pixels[y][x] = 0xFF000000 | shift(metal(tier, x, y), band(ring));
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

    public enum Face {
        TOP, SIDE, BOTTOM;

        private final String id = name().toLowerCase(Locale.ROOT);
    }

    public static List<Face> faces(Generator.Made made) {
        return made.source().flat() ? List.of(Face.TOP, Face.SIDE) : List.of(Face.values());
    }

    public static boolean lights(Generator.Made made, Face face) {
        return face == Face.TOP || face == Face.SIDE && !made.source().flat();
    }

    public static String generator(Generator.Made made, Face face, boolean lit) {
        String name = made.id() + "_" + face.id;
        return lit && lights(made, face) ? name + "_on" : name;
    }

    public static String kind(Kind kind, Tier tier) {
        return kind.id(tier);
    }

    public static String kind(Kind kind, Tier tier, Face face) {
        return kind.id(tier) + "_" + face.id;
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
            for (Face face : faces(made)) {
                names.add(generator(made, face, false));
                if (lights(made, face)) {
                    names.add(generator(made, face, true));
                }
            }
        }
        for (Kind kind : Kind.values()) {
            if (kind.carries()) {
                continue;
            }
            for (Tier tier : Tier.upTo(kind.top())) {
                for (Face face : Face.values()) {
                    names.add(kind(kind, tier, face));
                }
            }
        }
        for (Tier tier : Tier.upTo(Kind.highestCarried())) {
            names.add(arm(tier));
            names.add(drill(tier));
        }
        return names;
    }

    public static int[][] generatorSkin(Generator.Made made, Face face, boolean lit) {
        if (made.source().flat()) {
            return face == Face.TOP ? solarSkin(made.source(), made.tier(), lit) : plainSkin(made.tier());
        }
        Tier tier = made.tier() == null ? UNTIERED_METAL : made.tier();
        return switch (made.source()) {
            case ITEM -> burner(tier, face, lit);
            case FLUID -> crucible(tier, face, lit);
            case HEAT -> hypocaustum(tier, face, lit);
            case EXPERIENCE -> experientia(tier, face, lit);
            case LIFE -> spoliarium(tier, face, lit);
            case BLOW -> palus(tier, face, lit);
            case STORM -> bidental(tier, face, lit);
            case SUN, LAMP -> throw new IllegalStateException(made.id() + " is flat");
        };
    }

    private static final Tier UNTIERED_METAL = Tier.COPPER;

    private static final int SHINE = 0xFFFFFF;
    private static final int PATINA = 0x4FA88A;
    private static final int CAVITY = 0x1A1614;
    private static final int ASH = 0x3A3532;
    private static final int FIRE_CORE = 0xFFE08A;
    private static final int CRUST = 0x5A5560;

    private static final int BRICK = 0x94492F;
    private static final int BRICK_DARK = 0x7A3A25;
    private static final int MORTAR = 0x6B625A;
    private static final int TILE = 0x9A8E80;
    private static final int GROUT = 0x5E564E;
    private static final int CLAY = 0xA86A45;
    private static final int CLAY_DARK = 0x8A5436;
    private static final int OAK = 0x9C7447;
    private static final int OAK_DARK = 0x7A5832;
    private static final int OAK_END = 0xB48C5C;
    private static final int OAK_RING = 0x7E5E38;
    private static final int OAK_DENT = 0x5E4226;
    private static final int SPLINTER = 0xF0E2B8;
    private static final int STONE = 0x7D7D7A;
    private static final int STONE_DARK = 0x6A6A67;
    private static final int STONE_SEAM = 0x4E4E4C;
    private static final int SCORCH = 0x33302E;
    private static final int BLACKSTONE = 0x2F2A31;
    private static final int BLACKSTONE_LIGHT = 0x3B353E;
    private static final int BLACKSTONE_SEAM = 0x1A171C;
    private static final int GLASS = 0x1C2A24;
    private static final int GLASS_SHINE = 0x5A7A70;
    private static final int XP_COLD = 0x3A5222;
    private static final int XP_DEEP = 0x1E3012;
    private static final int XP_LIT = 0x9CF03A;
    private static final int XP_BUBBLE = 0xEAFFB8;
    private static final int BLOOD_COLD = 0x3A1418;
    private static final int BLOOD_LIT = 0xD01830;
    private static final int BOLT_LIT = 0xE4F8FF;
    private static final int BOLT_GLOW = 0x5AB8FF;

    private static final float METAL_MID = 0.78F;
    private static final float METAL_DARK = 0.55F;
    private static final float METAL_DEEP = 0.35F;
    private static final int BEVEL_LIT = 22;
    private static final int BEVEL_DARK = 30;

    private static final int PATINA_SHARE = 14;
    private static final float PATINA_DEPTH = 0.7F;
    private static final int BRUSH = 7;
    private static final int BRUSH_LENGTH = 5;
    private static final int POLISH_EVERY = 9;
    private static final int FACET = 4;
    private static final int SPARKLE_SHARE = 4;
    private static final int STREAK_LENGTH = 4;
    private static final int STREAK_EVERY = 7;
    private static final int GLEAM_PER_RUNG = 3;

    private static final int FLICKER = 18;
    private static final int CRACKS = 6;
    private static final float CLAY_HEAT = 0.25F;
    private static final int SCORCH_SHARE = 22;
    private static final int XP_LEVEL = 6;
    private static final int BUBBLES = 10;

    private enum Finish { PATINA, BRUSHED, POLISHED, FACETED, DARK, GLEAM }

    private static Finish finish(Tier tier) {
        return switch (tier) {
            case COPPER -> Finish.PATINA;
            case IRON -> Finish.BRUSHED;
            case GOLD -> Finish.POLISHED;
            case DIAMOND -> Finish.FACETED;
            case NETHERITE -> Finish.DARK;
            case NETHER_STAR, COMPRESSED_NETHER_STAR, SUPER_COMPRESSED_NETHER_STAR -> Finish.GLEAM;
        };
    }

    private static int metal(Tier tier, int x, int y) {
        int light = tier.colour();
        int mid = scale(light, METAL_MID);
        int dark = scale(light, METAL_DARK);
        int roll = hash(x, y) % 100;
        return switch (finish(tier)) {
            case PATINA -> roll < PATINA_SHARE ? mix(mid, PATINA, PATINA_DEPTH) : grain(mid, x, y);
            case BRUSHED -> shift(mid, hash(y, x / BRUSH_LENGTH) % (2 * BRUSH + 1) - BRUSH);
            case POLISHED -> {
                int band = Math.floorMod(x + y, POLISH_EVERY);
                yield band < 2 ? light : band == 2 ? mix(light, mid, 0.5F) : mid;
            }
            case FACETED -> roll < SPARKLE_SHARE ? SHINE
                    : Math.floorMod(x, FACET) > Math.floorMod(y, FACET) ? mix(light, mid, 0.4F) : mid;
            case DARK -> hash(y, x / STREAK_LENGTH) % STREAK_EVERY == 0 ? mid : dark;
            case GLEAM -> {
                int gleams = (tier.ordinal() - Tier.NETHER_STAR.ordinal() + 1) * GLEAM_PER_RUNG;
                yield roll < gleams ? SHINE : roll < 2 * gleams ? mix(light, SHINE, 0.4F) : mid;
            }
        };
    }

    @FunctionalInterface
    private interface Paint {
        int at(int x, int y);
    }

    private static void paint(int[][] pixels, int left, int top, int right, int bottom, Paint paint) {
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                pixels[y][x] = 0xFF000000 | paint.at(x, y);
            }
        }
    }

    private static void plated(int[][] pixels, Tier tier, int left, int top, int right, int bottom) {
        paint(pixels, left, top, right, bottom, (x, y) -> {
            int colour = metal(tier, x, y);
            if (y == top || x == left) {
                return shift(colour, BEVEL_LIT);
            }
            return y == bottom || x == right ? shift(colour, -BEVEL_DARK) : colour;
        });
    }

    private static void rivets(int[][] pixels, Tier tier) {
        int stud = mix(tier.colour(), SHINE, 0.5F);
        for (int y : new int[] { 2, SIZE - 3 }) {
            for (int x : new int[] { 2, SIZE - 3 }) {
                pixels[y][x] = 0xFF000000 | stud;
            }
        }
    }

    private static int fire(int x, int y, int top, int bottom) {
        float down = (y - top) / (float) Math.max(1, bottom - top);
        if (down > 0.5F && hash(x, y) % 100 < FLICKER) {
            return FIRE_CORE;
        }
        return mix(EMBER, MOUTH_LIT, down);
    }

    private static int molten(int x, int y) {
        int roll = hash(x, y) % 100;
        return roll < FLICKER ? FIRE_CORE : mix(EMBER, MOUTH_LIT, roll / 100.0F);
    }

    private static int brick(int x, int y) {
        int course = y / 3;
        int along = x + (course % 2) * 4;
        if (y % 3 == 2 || along % 8 == 7) {
            return MORTAR;
        }
        return grain(hash(along / 8, course) % 3 == 0 ? BRICK_DARK : BRICK, x, y);
    }

    private static int blocks(int x, int y, int face, int shade, int seam) {
        int course = y / 5;
        int along = x + (course % 2) * 4;
        if (y % 5 == 4 || along % 8 == 7) {
            return seam;
        }
        return grain(hash(along / 8, course) % 3 == 0 ? shade : face, x, y);
    }

    private static int stone(int x, int y) {
        return blocks(x, y, STONE, STONE_DARK, STONE_SEAM);
    }

    private static int blackstone(int x, int y) {
        return blocks(x, y, BLACKSTONE, BLACKSTONE_LIGHT, BLACKSTONE_SEAM);
    }

    private static int clay(int x, int y) {
        return grain(hash(x / 2, y / 2) % 5 == 0 ? CLAY_DARK : CLAY, x, y);
    }

    private static int oak(int x, int y) {
        return x % 4 == (hash(x / 4, y / 6) % 2) ? OAK_DARK : grain(OAK, x, y);
    }

    private static int endGrain(int x, int y) {
        float middle = (SIZE - 1) / 2.0F;
        int ring = (int) Math.hypot(x - middle, y - middle);
        return ring % 3 == 0 ? OAK_RING : grain(OAK_END, x, y);
    }

    private static int[][] burner(Tier tier, Face face, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        plated(pixels, tier, 0, 0, 15, 15);
        int slot = scale(tier.colour(), METAL_DEEP);
        switch (face) {
            case SIDE -> {
                paint(pixels, 3, 2, 12, 2, (x, y) -> slot);
                paint(pixels, 3, 4, 12, 4, (x, y) -> slot);
                plated(pixels, tier, 2, 6, 13, 14);
                paint(pixels, 3, 7, 12, 13, (x, y) -> x % 3 == 2 ? scale(tier.colour(), METAL_DARK)
                        : lit ? fire(x, y, 7, 13) : y == 13 ? ASH : CAVITY);
            }
            case TOP -> {
                plated(pixels, tier, 4, 4, 11, 11);
                paint(pixels, 6, 6, 9, 9, (x, y) -> lit ? fire(x, y, 6, 9) : CAVITY);
            }
            case BOTTOM -> rivets(pixels, tier);
        }
        return pixels;
    }

    private static int[][] crucible(Tier tier, Face face, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        switch (face) {
            case SIDE -> {
                paint(pixels, 0, 0, 15, 15, (x, y) -> {
                    int body = lit ? mix(clay(x, y), EMBER, CLAY_HEAT) : clay(x, y);
                    return lit && y > 3 && y < 12 && hash(x, y) % 100 < CRACKS ? MOUTH_LIT : body;
                });
                plated(pixels, tier, 0, 1, 15, 3);
                plated(pixels, tier, 0, 12, 15, 14);
            }
            case TOP -> {
                plated(pixels, tier, 0, 0, 15, 15);
                paint(pixels, 2, 2, 13, 13, Skins::clay);
                paint(pixels, 3, 3, 12, 12, (x, y) -> lit ? molten(x, y)
                        : hash(x, y) % 100 < FLICKER ? CRUST : MOLTEN_COLD);
            }
            case BOTTOM -> paint(pixels, 0, 0, 15, 15, Skins::clay);
        }
        return pixels;
    }

    private static int[][] hypocaustum(Tier tier, Face face, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        switch (face) {
            case SIDE -> {
                paint(pixels, 0, 0, 15, 8, Skins::brick);
                plated(pixels, tier, 0, 9, 15, 10);
                paint(pixels, 0, 11, 15, 15, (x, y) -> {
                    boolean pila = x <= 2 || x >= 6 && x <= 9 || x >= 13;
                    if (pila) {
                        return y % 2 == 0 ? MORTAR : grain(BRICK, x, y);
                    }
                    return lit ? fire(x, y, 11, 15) : CAVITY;
                });
            }
            case TOP -> paint(pixels, 0, 0, 15, 15, (x, y) -> x % 8 == 0 || y % 8 == 0
                    ? (lit ? mix(GROUT, EMBER, 0.6F) : GROUT)
                    : grain(hash(x / 8, y / 8) % 2 == 0 ? TILE : shift(TILE, -10), x, y));
            case BOTTOM -> paint(pixels, 0, 0, 15, 15, Skins::brick);
        }
        return pixels;
    }

    private static int[][] experientia(Tier tier, Face face, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        plated(pixels, tier, 0, 0, 15, 15);
        switch (face) {
            case SIDE -> paint(pixels, 3, 2, 12, 13, (x, y) -> {
                boolean shine = x - y == 4 || x - y == 5;
                if (y < XP_LEVEL) {
                    return shine ? GLASS_SHINE : GLASS;
                }
                float down = (y - XP_LEVEL) / (float) (13 - XP_LEVEL);
                int liquid = lit
                        ? (hash(x, y) % 100 < BUBBLES ? XP_BUBBLE : mix(XP_LIT, XP_COLD, down))
                        : mix(XP_COLD, XP_DEEP, down);
                return shine ? shift(liquid, 24) : liquid;
            });
            case TOP -> {
                plated(pixels, tier, 3, 3, 12, 12);
                paint(pixels, 5, 5, 10, 10, (x, y) -> scale(tier.colour(), METAL_DEEP));
                paint(pixels, 7, 7, 8, 8, (x, y) -> lit ? XP_LIT : XP_COLD);
            }
            case BOTTOM -> rivets(pixels, tier);
        }
        return pixels;
    }

    private static int[][] spoliarium(Tier tier, Face face, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        switch (face) {
            case SIDE -> {
                paint(pixels, 0, 0, 15, 15, Skins::blackstone);
                plated(pixels, tier, 0, 0, 15, 2);
                paint(pixels, 7, 3, 8, 15, (x, y) -> lit
                        ? (y % 3 == 0 ? shift(BLOOD_LIT, -40) : BLOOD_LIT) : BLOOD_COLD);
            }
            case TOP -> {
                plated(pixels, tier, 0, 0, 15, 15);
                paint(pixels, 2, 2, 13, 13, (x, y) -> (x - 2) % 3 == 2 || y == 7 || y == 8
                        ? metal(tier, x, y)
                        : lit ? BLOOD_LIT : BLACKSTONE_SEAM);
            }
            case BOTTOM -> paint(pixels, 0, 0, 15, 15, Skins::blackstone);
        }
        return pixels;
    }

    private static int[][] palus(Tier tier, Face face, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        switch (face) {
            case SIDE -> {
                paint(pixels, 0, 0, 15, 15, (x, y) -> y >= 5 && y <= 10 && x >= 3 && x <= 12
                        && (x + y) % 6 == 0 ? (lit ? SPLINTER : OAK_DENT) : oak(x, y));
                plated(pixels, tier, 0, 2, 15, 3);
                plated(pixels, tier, 0, 12, 15, 13);
            }
            case TOP, BOTTOM -> {
                plated(pixels, tier, 0, 0, 15, 15);
                paint(pixels, 1, 1, 14, 14, Skins::endGrain);
            }
        }
        return pixels;
    }

    private static int[][] bidental(Tier tier, Face face, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        switch (face) {
            case SIDE -> {
                paint(pixels, 0, 0, 15, 15, (x, y) -> y < 8 && hash(x, y) % 100 < SCORCH_SHARE
                        ? SCORCH : stone(x, y));
                plated(pixels, tier, 0, 0, 15, 1);
                for (int y = 2; y < SIZE; y++) {
                    int at = 6 + (y / 2) % 3;
                    pixels[y][at] = 0xFF000000 | (lit ? BOLT_LIT : SCORCH);
                    if (lit) {
                        pixels[y][at - 1] = 0xFF000000 | mix(stone(at - 1, y), BOLT_GLOW, 0.6F);
                        pixels[y][at + 1] = 0xFF000000 | mix(stone(at + 1, y), BOLT_GLOW, 0.6F);
                    }
                }
            }
            case TOP -> {
                paint(pixels, 0, 0, 15, 15, Skins::stone);
                plated(pixels, tier, 7, 0, 8, 15);
                plated(pixels, tier, 0, 7, 15, 8);
                plated(pixels, tier, 5, 5, 10, 10);
                if (lit) {
                    paint(pixels, 7, 7, 8, 8, (x, y) -> BOLT_LIT);
                    paint(pixels, 6, 6, 9, 6, (x, y) -> BOLT_GLOW);
                    paint(pixels, 6, 9, 9, 9, (x, y) -> BOLT_GLOW);
                }
            }
            case BOTTOM -> paint(pixels, 0, 0, 15, 15, Skins::stone);
        }
        return pixels;
    }

    private static int hash(int x, int y) {
        int h = x * 73856093 ^ y * 19349663;
        h ^= h >>> 13;
        h *= 0x5BD1E995;
        return (h ^ h >>> 15) & 0x7FFFFFFF;
    }

    private static int scale(int colour, float by) {
        return Math.round(((colour >> 16) & 0xFF) * by) << 16
                | Math.round(((colour >> 8) & 0xFF) * by) << 8
                | Math.round((colour & 0xFF) * by);
    }

    public static int[][] kindSkin(Kind kind, Tier tier, Face face) {
        return switch (kind) {
            case BATTERY -> battery(tier, face);
            case CHARGER -> charger(tier, face);
            case CABLE, IMPORTER, EXPORTER -> throw new IllegalStateException(kind + " carries");
        };
    }

    private static final int INSULATOR = 0x2A2D33;
    private static final int CELL_LIFT = 18;
    private static final int CELL_SINK = 14;
    private static final int CELL_EVERY = 4;
    private static final int COIL = 0xB8663A;
    private static final int COIL_DARK = 0x7A3F22;
    private static final float CONTACT_SHINE = 0.45F;

    private static int[][] battery(Tier tier, Face face) {
        int[][] pixels = new int[SIZE][SIZE];
        plated(pixels, tier, 0, 0, 15, 15);
        switch (face) {
            case SIDE -> paint(pixels, 2, 3, 13, 12, (x, y) -> switch ((x - 2) % CELL_EVERY) {
                case 0 -> shift(metal(tier, x, y), BEVEL_LIT);
                case 1 -> grain(shift(INSULATOR, CELL_LIFT), x, y);
                case 2 -> grain(INSULATOR, x, y);
                default -> grain(shift(INSULATOR, -CELL_SINK), x, y);
            });
            case TOP -> {
                paint(pixels, 2, 2, 13, 13, (x, y) -> grain(INSULATOR, x, y));
                plated(pixels, tier, 4, 6, 6, 9);
                plated(pixels, tier, 9, 6, 11, 9);
            }
            case BOTTOM -> rivets(pixels, tier);
        }
        return pixels;
    }

    private static int[][] charger(Tier tier, Face face) {
        int[][] pixels = new int[SIZE][SIZE];
        plated(pixels, tier, 0, 0, 15, 15);
        switch (face) {
            case SIDE -> {
                paint(pixels, 1, 5, 14, 10, (x, y) -> x == 1 || x == 14 ? INSULATOR
                        : grain(y % 2 == 0 ? COIL : COIL_DARK, x, y));
                paint(pixels, 1, 4, 14, 4, (x, y) -> scale(tier.colour(), METAL_DEEP));
            }
            case TOP -> {
                for (int top : new int[] { 2, 9 }) {
                    for (int left : new int[] { 2, 9 }) {
                        int right = left + 4;
                        int bottom = top + 4;
                        paint(pixels, left, top, right, bottom, (x, y) -> x == left || y == top
                                ? shift(INSULATOR, -CELL_SINK) : grain(INSULATOR, x, y));
                        paint(pixels, left + 2, top + 2, left + 3, top + 3,
                                (x, y) -> mix(metal(tier, x, y), SHINE, CONTACT_SHINE));
                    }
                }
            }
            case BOTTOM -> rivets(pixels, tier);
        }
        return pixels;
    }

    /**
     * The face of a panel: cells behind glass in a frame.
     *
     * <p>Four rows of four, so the grid reads at a glance and still fits the frame,
     * and each cell is lighter towards its top left - a flat blue square looks
     * painted on, while a sheen looks like something under glass.
     */
    public static int[][] solarSkin(Source source, Tier tier, boolean lit) {
        int[][] pixels = new int[SIZE][SIZE];
        // ⭐ The glass is the same glass on every rung; what the rung changes is the
        // cast of it. Mixing towards the metal keeps one picture for all of them and
        // still tells them apart from above, which is the only side anybody sees.
        int cell = source == Source.LAMP
                ? (lit ? LAMP_CELL_WORKING : LAMP_CELL_RESTING)
                : (lit ? CELL_WORKING : CELL_RESTING);
        int base = mix(cell, tier.colour(), TIER_TINT);
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
                pixels[y][x] = 0xFF000000 | SolPalette.colour(heat, 0.0F);
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
        int[][] pixels = new int[SIZE][SIZE];
        plated(pixels, tier, 0, 0, SIZE - 1, SIZE - 1);
        return pixels;
    }


    /** How far the pictures are pulled towards the metal of the rung they are on. */
    private static final float TIER_TINT = 0.34F;

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
    public static final int POUR_X = 8;
    public static final int EXPERIENCE_Y = 30;
    public static final int POUR_SPAN = 140;
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
