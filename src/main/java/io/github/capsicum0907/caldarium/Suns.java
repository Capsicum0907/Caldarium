package io.github.capsicum0907.caldarium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Suns {
    private static final Map<Level, Set<BlockPos>> PLACED = Collections.synchronizedMap(new WeakHashMap<>());

    private static final double SHELL_TOLERANCE = 1.0;

    private Suns() {
    }

    public static void add(Level level, BlockPos pos) {
        PLACED.computeIfAbsent(level, key -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
    }

    public static void remove(Level level, BlockPos pos) {
        Set<BlockPos> placed = PLACED.get(level);
        if (placed != null) {
            placed.remove(pos);
        }
    }

    private static List<BlockPos> in(Level level) {
        Set<BlockPos> placed = PLACED.get(level);
        if (placed == null || placed.isEmpty()) {
            return List.of();
        }
        List<BlockPos> standing = new ArrayList<>();
        for (BlockPos pos : placed) {
            if (level.isLoaded(pos) && level.getBlockState(pos).getBlock() instanceof SolBlock) {
                standing.add(pos);
            }
        }
        return standing;
    }

    public static List<VoxelShape> touching(Level level, AABB box) {
        List<BlockPos> suns = in(level);
        if (suns.isEmpty()) {
            return List.of();
        }
        List<VoxelShape> shapes = new ArrayList<>();
        for (BlockPos pos : suns) {
            BlockState state = level.getBlockState(pos);
            if (SolBlock.bounds(state, pos).intersects(box)) {
                shapes.addAll(SolBlock.slabs(SolBlock.radius(state), SolBlock.centre(pos), box));
            }
        }
        return shapes;
    }

    public static BlockHitResult clip(Level level, Vec3 from, Vec3 to) {
        BlockHitResult nearest = null;
        double best = Double.MAX_VALUE;
        AABB path = new AABB(from, to);
        for (BlockPos pos : in(level)) {
            BlockState state = level.getBlockState(pos);
            if (!SolBlock.bounds(state, pos).intersects(path)) {
                continue;
            }
            BlockHitResult hit = SolBlock.hit(state, pos, from, to);
            if (hit != null && from.distanceToSqr(hit.getLocation()) < best) {
                best = from.distanceToSqr(hit.getLocation());
                nearest = hit;
            }
        }
        return nearest;
    }

    public static boolean lights(Level level, BlockPos cell) {
        Vec3 point = Vec3.atCenterOf(cell);
        for (BlockPos pos : in(level)) {
            double shell = SolBlock.shell(SolBlock.radius(level.getBlockState(pos)));
            if (Math.abs(point.distanceTo(SolBlock.centre(pos)) - shell) <= SHELL_TOLERANCE) {
                return true;
            }
        }
        return false;
    }

    public static boolean shining(Level level, BlockPos cell) {
        Vec3 point = Vec3.atCenterOf(cell);
        int reach = CaldariumConfig.solReach();
        for (BlockPos pos : in(level)) {
            if (SolBlock.gap(level.getBlockState(pos), pos, point) <= reach) {
                return true;
            }
        }
        return false;
    }

    public static boolean inside(Level level, BlockPos cell) {
        Vec3 point = Vec3.atCenterOf(cell);
        for (BlockPos pos : in(level)) {
            if (!pos.equals(cell) && SolBlock.gap(level.getBlockState(pos), pos, point) <= 0.0) {
                return true;
            }
        }
        return false;
    }
}
