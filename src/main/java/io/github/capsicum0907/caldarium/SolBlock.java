package io.github.capsicum0907.caldarium;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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

public class SolBlock extends BaseEntityBlock {
    /** How far through its durability it is, in quarters. Only the light reads it. */
    public static final IntegerProperty SPENT = IntegerProperty.create("spent", 0, 3);

    public static final MapCodec<SolBlock> CODEC = simpleCodec(SolBlock::new);

    private static final int BRIGHTEST = 15;
    private static final int QUARTERS = 4;
    private static final float SHRUNK = 0.55F;

    public SolBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPENT, 0));
    }

    /** Light for a quarter of its life spent, so it visibly dims as it goes. */
    public static int light(BlockState state) {
        return Math.max(1, BRIGHTEST - state.getValue(SPENT) * 3);
    }

    public static float fullRadius() {
        return CaldariumConfig.solSize() / 2.0F;
    }

    public static float radius(BlockState state) {
        float left = 1.0F - state.getValue(SPENT) / (float) QUARTERS;
        return fullRadius() * (SHRUNK + (1.0F - SHRUNK) * left);
    }

    public static int span() {
        return (int) Math.ceil(fullRadius());
    }

    public static List<BlockPos> body(BlockPos core, float radius) {
        List<BlockPos> cells = new ArrayList<>();
        int span = (int) Math.ceil(radius);
        float limit = radius * radius;
        for (int dx = -span; dx <= span; dx++) {
            for (int dy = -span; dy <= span; dy++) {
                for (int dz = -span; dz <= span; dz++) {
                    if ((dx != 0 || dy != 0 || dz != 0) && dx * dx + dy * dy + dz * dz <= limit) {
                        cells.add(core.offset(dx, dy, dz));
                    }
                }
            }
        }
        return cells;
    }

    public static BlockPos coreOf(BlockGetter level, BlockPos cell) {
        int span = span();
        for (BlockPos at : BlockPos.betweenClosed(cell.offset(-span, -span, -span),
                cell.offset(span, span, span))) {
            BlockState state = level.getBlockState(at);
            if (state.getBlock() instanceof SolBlock) {
                float radius = radius(state);
                if (at.distSqr(cell) <= radius * radius) {
                    return at.immutable();
                }
            }
        }
        return null;
    }

    public static float progress(BlockState state, Player player, BlockGetter level, BlockPos pos,
            float otherwise) {
        if (!(level instanceof Level world) || !player.hasCorrectToolForDrops(state, world, pos)) {
            return 0.0F;
        }
        return otherwise;
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        int apart = span() * 2 + 1;
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-apart, -apart, -apart),
                pos.offset(apart, apart, apart))) {
            if (context.getLevel().getBlockState(at).getBlock() instanceof SolBlock) {
                return null;
            }
        }
        return defaultBlockState();
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (level.isClientSide() || old.is(this)) {
            return;
        }
        BlockState filler = CaldariumRegistry.SOL_BODY.get().defaultBlockState()
                .setValue(SPENT, state.getValue(SPENT));
        for (BlockPos cell : body(pos, radius(state))) {
            if (level.getBlockState(cell).canBeReplaced()) {
                level.setBlock(cell, filler, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState now, boolean moved) {
        if (!level.isClientSide()) {
            float keep = now.is(this) ? radius(now) : -1.0F;
            float limit = keep * keep;
            for (BlockPos cell : body(pos, Math.max(fullRadius(), radius(state)))) {
                BlockState there = level.getBlockState(cell);
                if (!(there.getBlock() instanceof SolBodyBlock)) {
                    continue;
                }
                if (keep >= 0.0F && cell.distSqr(pos) <= limit) {
                    level.setBlock(cell, there.setValue(SPENT, now.getValue(SPENT)), Block.UPDATE_ALL);
                } else {
                    level.setBlock(cell, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        super.onRemove(state, level, pos, now, moved);
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
