package io.github.capsicum0907.caldarium;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
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

    public SolBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPENT, 0));
    }

    /** Light for a quarter of its life spent, so it visibly dims as it goes. */
    public static int light(BlockState state) {
        return Math.max(1, BRIGHTEST - state.getValue(SPENT) * 3);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(SPENT);
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
    protected float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player,
            BlockGetter level, BlockPos pos) {
        if (!(level instanceof net.minecraft.world.level.Level world)
                || !player.hasCorrectToolForDrops(state, world, pos)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
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
