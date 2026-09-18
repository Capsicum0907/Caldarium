package io.github.capsicum0907.caldarium;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SolBlock extends BaseEntityBlock {
    /** How far through its durability it is, in quarters. Only the light reads it. */
    public static final IntegerProperty SPENT = IntegerProperty.create("spent", 0, 3);

    public static final MapCodec<SolBlock> CODEC = simpleCodec(SolBlock::new);

    private static final int BRIGHTEST = 15;
    private static final int QUARTERS = 4;
    private static final float SHRUNK = 0.55F;
    private static final int SLICES = 16;
    private static final double STEP = 0.25;
    public static final double SKIN = STEP;
    private static final float BLAST_VOLUME = 4.0F;
    private static final double GLOW_OUTSIDE = 1.0;
    private static final int MIN_GLOWS = 6;
    private static final int CLEARING = 2;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));
    private static final Map<Float, VoxelShape> SHAPES = new ConcurrentHashMap<>();

    public SolBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPENT, 0));
    }

    /** Light for a quarter of its life spent, so it visibly dims as it goes. */
    public static int light(BlockState state) {
        return BRIGHTEST;
    }

    public static float radius(BlockState state) {
        float left = 1.0F - state.getValue(SPENT) / (float) QUARTERS;
        return CaldariumConfig.solSize() / 2.0F * (SHRUNK + (1.0F - SHRUNK) * left);
    }

    public static Vec3 centre(BlockPos pos) {
        return Vec3.atCenterOf(pos);
    }

    public static double gap(BlockState state, BlockPos pos, Vec3 point) {
        return Math.max(0.0, point.distanceTo(centre(pos)) - radius(state));
    }

    public static AABB bounds(BlockState state, BlockPos pos) {
        return AABB.ofSize(centre(pos), 0.0, 0.0, 0.0).inflate(radius(state));
    }

    public static BlockHitResult hit(BlockState state, BlockPos pos, Vec3 from, Vec3 to) {
        Vec3 along = to.subtract(from);
        Vec3 offset = from.subtract(centre(pos));
        double radius = radius(state);
        double a = along.lengthSqr();
        double b = 2.0 * offset.dot(along);
        double c = offset.lengthSqr() - radius * radius;
        if (a == 0.0) {
            return null;
        }
        if (c <= 0.0) {
            return new BlockHitResult(from, Direction.getNearest(-along.x, -along.y, -along.z), pos, true);
        }
        double disc = b * b - 4.0 * a * c;
        if (disc < 0.0) {
            return null;
        }
        double t = (-b - Math.sqrt(disc)) / (2.0 * a);
        if (t < 0.0 || t > 1.0) {
            return null;
        }
        Vec3 point = from.add(along.scale(t));
        Vec3 normal = point.subtract(centre(pos));
        return new BlockHitResult(point, Direction.getNearest(normal.x, normal.y, normal.z), pos, false);
    }

    public static List<VoxelShape> slabs(double radius, Vec3 centre, AABB region) {
        List<VoxelShape> slabs = new ArrayList<>();
        double lowX = Math.max(region.minX, centre.x - radius);
        double highX = Math.min(region.maxX, centre.x + radius);
        double lowY = Math.max(region.minY, centre.y - radius);
        double highY = Math.min(region.maxY, centre.y + radius);
        if (lowX >= highX || lowY >= highY) {
            return slabs;
        }
        for (double y = Math.floor(lowY / STEP) * STEP; y < highY; y += STEP) {
            double dy = y + STEP / 2.0 - centre.y;
            for (double x = Math.floor(lowX / STEP) * STEP; x < highX; x += STEP) {
                double dx = x + STEP / 2.0 - centre.x;
                double left = radius * radius - dx * dx - dy * dy;
                if (left <= 0.0) {
                    continue;
                }
                double half = Math.sqrt(left);
                double near = Math.max(centre.z - half, region.minZ - STEP);
                double far = Math.min(centre.z + half, region.maxZ + STEP);
                if (near < far) {
                    slabs.add(Shapes.create(x, y, near, x + STEP, y + STEP, far));
                }
            }
        }
        return slabs;
    }

    public static VoxelShape shape(BlockState state) {
        return SHAPES.computeIfAbsent(radius(state), SolBlock::ball);
    }

    private static VoxelShape ball(float radius) {
        double step = radius * 2.0 / SLICES;
        VoxelShape ball = Shapes.empty();
        for (int i = 0; i < SLICES; i++) {
            double y = -radius + (i + 0.5) * step;
            for (int j = 0; j < SLICES; j++) {
                double x = -radius + (j + 0.5) * step;
                double left = radius * radius - x * x - y * y;
                if (left <= 0.0) {
                    continue;
                }
                double half = Math.sqrt(left);
                double x0 = -radius + j * step;
                double y0 = -radius + i * step;
                ball = Shapes.joinUnoptimized(ball, Shapes.create(
                        0.5 + x0, 0.5 + y0, 0.5 - half, 0.5 + x0 + step, 0.5 + y0 + step, 0.5 + half),
                        net.minecraft.world.phys.shapes.BooleanOp.OR);
            }
        }
        return ball.optimize();
    }

    public static double shell(float radius) {
        return radius + GLOW_OUTSIDE;
    }

    public static List<BlockPos> glowCells(BlockPos core, float radius) {
        double shell = shell(radius);
        int spacing = Math.max(1, CaldariumConfig.solGlowSpacing());
        int count = Math.max(MIN_GLOWS, (int) Math.ceil(4.0 * Math.PI * shell * shell / (spacing * spacing)));
        Vec3 centre = centre(core);
        Set<BlockPos> cells = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            double y = 1.0 - 2.0 * (i + 0.5) / count;
            double across = Math.sqrt(1.0 - y * y);
            double turn = i * GOLDEN_ANGLE;
            Vec3 at = centre.add(Math.cos(turn) * across * shell, y * shell, Math.sin(turn) * across * shell);
            cells.add(BlockPos.containing(at));
        }
        return new ArrayList<>(cells);
    }

    public static List<BlockPos> groundCells(Level level, BlockPos core, float radius) {
        int reach = CaldariumConfig.solReach();
        Vec3 centre = centre(core);
        List<BlockPos> cells = new ArrayList<>();
        for (BlockPos column : groundColumns(core, radius)) {
            if (!level.isLoaded(column)) {
                continue;
            }
            BlockPos cell = ground(level, centre, radius, reach, column.getX(), column.getZ());
            if (cell != null && Vec3.atCenterOf(cell).distanceTo(centre) - radius <= reach) {
                cells.add(cell);
            }
        }
        return cells;
    }

    private static List<BlockPos> groundColumns(BlockPos core, float radius) {
        int spacing = Math.max(1, CaldariumConfig.solGroundSpacing());
        double far = radius + CaldariumConfig.solReach();
        int steps = (int) Math.floor(far / spacing);
        List<BlockPos> columns = new ArrayList<>();
        for (int i = -steps; i <= steps; i++) {
            for (int k = -steps; k <= steps; k++) {
                if (Math.hypot(i * spacing, k * spacing) <= far) {
                    columns.add(core.offset(i * spacing, 0, k * spacing));
                }
            }
        }
        return columns;
    }

    private static BlockPos ground(Level level, Vec3 centre, float radius, int reach, int x, int z) {
        double far = radius + reach;
        int lowest = Math.max(level.getMinBuildHeight(), (int) Math.floor(centre.y - far));
        int highest = Math.min(level.getMaxBuildHeight() - 1, (int) Math.ceil(centre.y + far));
        double shell = radius + GLOW_OUTSIDE;
        double across = Math.hypot(x + 0.5 - centre.x, z + 0.5 - centre.z);
        double under = across < shell ? Math.sqrt(shell * shell - across * across) : 0.0;
        int start = (int) Math.floor(centre.y - under);
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(x, start, z);
        int found = Integer.MIN_VALUE;
        if (!solid(level.getBlockState(at))) {
            for (int y = start; y > lowest; y--) {
                if (solid(level.getBlockState(at.setY(y - 1)))) {
                    found = y;
                    break;
                }
            }
        } else {
            for (int y = start + 1; y <= highest; y++) {
                if (!solid(level.getBlockState(at.setY(y)))) {
                    found = y;
                    break;
                }
            }
        }
        if (found == Integer.MIN_VALUE) {
            return null;
        }
        for (int y = found; y <= Math.min(highest, found + CLEARING); y++) {
            BlockState there = level.getBlockState(at.setY(y));
            if (there.isAir() || there.getBlock() instanceof SolGlowBlock) {
                return at.immutable();
            }
        }
        return null;
    }

    private static boolean solid(BlockState state) {
        return state.blocksMotion() || !state.getFluidState().isEmpty();
    }

    private static List<BlockPos> columnGlows(Level level, BlockPos core, float radius) {
        int far = (int) Math.ceil(radius + CaldariumConfig.solReach()) + 1;
        List<BlockPos> glows = new ArrayList<>();
        for (BlockPos column : groundColumns(core, radius)) {
            if (!level.isLoaded(column)) {
                continue;
            }
            for (int y = -far; y <= far; y++) {
                BlockPos cell = column.above(y);
                if (level.getBlockState(cell).getBlock() instanceof SolGlowBlock) {
                    glows.add(cell);
                }
            }
        }
        return glows;
    }

    public static void glow(Level level, BlockPos core, BlockState state) {
        BlockState glow = CaldariumRegistry.SOL_GLOW.get().defaultBlockState().setValue(SPENT, state.getValue(SPENT));
        float radius = radius(state);
        Set<BlockPos> wanted = new LinkedHashSet<>(glowCells(core, radius));
        wanted.addAll(groundCells(level, core, radius));
        for (BlockPos stale : columnGlows(level, core, radius)) {
            if (!wanted.contains(stale)) {
                level.setBlock(stale, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        for (BlockPos cell : wanted) {
            if (!level.isLoaded(cell)) {
                continue;
            }
            BlockState there = level.getBlockState(cell);
            if (there.isAir() || (there.getBlock() instanceof SolGlowBlock && !there.equals(glow))) {
                level.setBlock(cell, glow, Block.UPDATE_ALL);
            }
        }
    }

    public static void unglow(Level level, BlockPos core, BlockState state) {
        float radius = radius(state);
        List<BlockPos> cells = new ArrayList<>(glowCells(core, radius));
        cells.addAll(columnGlows(level, core, radius));
        for (BlockPos cell : cells) {
            if (level.isLoaded(cell) && level.getBlockState(cell).getBlock() instanceof SolGlowBlock) {
                level.setBlock(cell, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!level.isClientSide() && !old.is(this)) {
            glow(level, pos, state);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState now, boolean moved) {
        if (!level.isClientSide()) {
            unglow(level, pos, state);
            if (now.is(this)) {
                glow(level, pos, now);
            }
        }
        super.onRemove(state, level, pos, now, moved);
    }

    public static float progress(BlockState state, Player player, BlockGetter level, BlockPos pos,
            float otherwise) {
        if (!(level instanceof Level world) || !player.hasCorrectToolForDrops(state, world, pos)) {
            return 0.0F;
        }
        return otherwise;
    }

    public static void blast(ServerLevel level, BlockPos pos, BlockState state, Player breaker) {
        double reach = radius(state) + CaldariumConfig.solBlastReach();
        Vec3 centre = centre(pos);
        AABB around = new AABB(pos).inflate(Math.ceil(reach));
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, around,
                living -> living.getBoundingBox().getCenter().distanceTo(centre) <= reach)) {
            living.hurt(level.damageSources().explosion(breaker, breaker), CaldariumConfig.solBlastDamage());
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, centre.x, centre.y, centre.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, BLAST_VOLUME, 1.0F);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel server) {
            blast(server, pos, state, player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(SPENT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    /**
     * ⚠ <b>The wrong tool does not make it slow, it makes it impossible.</b> No progress
     * at all rather than a long wait: a sun you could get through with a stone pickaxe
     * given the patience is a sun anybody gets through.
     *
     * <p>Creative is untouched, because breaking a block there never asks about
     * progress in the first place.
     */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return progress(state, player, level, pos, super.getDestroyProgress(state, player, level, pos));
    }

    /** ⚠ The sphere is drawn by {@code SolRenderer}; a cube inside it would show. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, CaldariumRegistry.SOL_ENTITY.get(),
                        SolBlockEntity::serverTick);
    }

    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType path) {
        return false;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
