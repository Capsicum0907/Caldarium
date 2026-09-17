package io.github.capsicum0907.caldarium;

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
    private static final float BLAST_VOLUME = 4.0F;
    private static final Map<Float, VoxelShape> SHAPES = new ConcurrentHashMap<>();

    public SolBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPENT, 0));
    }

    /** Light for a quarter of its life spent, so it visibly dims as it goes. */
    public static int light(BlockState state) {
        return Math.max(1, BRIGHTEST - state.getValue(SPENT) * 3);
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
