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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class SolItem extends BlockItem {
    public SolItem(Block block, Properties properties) {
        super(block, properties);
    }

    private static final double HALF_DIAGONAL = Math.sqrt(3.0) / 2.0;
    private static final int STEPS = 32;

    public static BlockPos core(Player player, BlockState state) {
        double want = SolBlock.radius(state) + CaldariumConfig.solHold() + HALF_DIAGONAL;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB body = player.getBoundingBox();
        double near = 0.0;
        double far = want + body.getSize() * 2.0;
        for (int step = 0; step < STEPS; step++) {
            double middle = (near + far) / 2.0;
            if (gap(body, eye.add(look.scale(middle))) < want) {
                near = middle;
            } else {
                far = middle;
            }
        }
        return BlockPos.containing(eye.add(look.scale(far)));
    }

    public static double gap(AABB body, Vec3 point) {
        double x = Math.max(Math.max(body.minX - point.x, 0.0), point.x - body.maxX);
        double y = Math.max(Math.max(body.minY - point.y, 0.0), point.y - body.maxY);
        double z = Math.max(Math.max(body.minZ - point.z, 0.0), point.z - body.maxZ);
        return Math.sqrt(x * x + y * y + z * z);
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
