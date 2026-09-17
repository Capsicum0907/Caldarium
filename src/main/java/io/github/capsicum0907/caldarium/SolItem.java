package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class SolItem extends BlockItem {
    public SolItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static BlockPos core(BlockPlaceContext context, BlockState state) {
        int apart = Math.max(0, (int) Math.ceil(SolBlock.radius(state) - 0.5F));
        return context.getClickedPos().relative(context.getClickedFace(), apart);
    }

    public static boolean fits(BlockPlaceContext context, BlockState state) {
        BlockPos core = core(context, state);
        BlockPlaceContext moved = BlockPlaceContext.at(context, core, context.getClickedFace());
        CollisionContext who = context.getPlayer() == null
                ? CollisionContext.empty() : CollisionContext.of(context.getPlayer());
        return moved.canPlace()
                && !Suns.inside(context.getLevel(), core)
                && context.getLevel().isUnobstructed(state, core, who);
    }

    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        return BlockPlaceContext.at(context, core(context, getBlock().defaultBlockState()),
                context.getClickedFace());
    }
}
