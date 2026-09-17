package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class SolItem extends BlockItem {
    public SolItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static BlockPos core(Player player, BlockState state) {
        double ahead = SolBlock.radius(state) + CaldariumConfig.solHold();
        return BlockPos.containing(player.getEyePosition().add(player.getLookAngle().scale(ahead)));
    }

    public static boolean fits(Level level, Player player, BlockPos core, BlockState state) {
        return level.isInWorldBounds(core)
                && level.getBlockState(core).canBeReplaced()
                && !Suns.inside(level, core)
                && level.isUnobstructed(state, core, CollisionContext.of(player));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockPos core = core(player, getBlock().defaultBlockState());
        if (!level.isInWorldBounds(core)) {
            return InteractionResultHolder.pass(held);
        }
        Vec3 look = player.getLookAngle();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(core),
                Direction.getNearest(-look.x, -look.y, -look.z), core, false);
        InteractionResult placed = held.useOn(new UseOnContext(player, hand, hit));
        return new InteractionResultHolder<>(placed, held);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !new BlockPlaceContext(context).getClickedPos()
                .equals(core(player, getBlock().defaultBlockState()))) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }
}
