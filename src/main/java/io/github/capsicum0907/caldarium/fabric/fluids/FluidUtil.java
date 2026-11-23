package io.github.capsicum0907.caldarium.fabric.fluids;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
//? if >=1.19 {
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
//?} else {
/*import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
*///?}
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class FluidUtil {
    private FluidUtil() {
    }

    public static Optional<Storage<FluidVariant>> getFluidHandler(ItemStack stack) {
        return Optional.ofNullable(FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack)));
    }

    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos,
            Direction side) {
        Storage<FluidVariant> target = FluidStorage.SIDED.find(level, pos, side);
        //? if >=1.19 {
        return target != null && FluidStorageUtil.interactWithFluidStorage(target, player, hand);
        //?} else {
        /*Storage<FluidVariant> held = FluidStorage.ITEM.find(player.getItemInHand(hand),
                ContainerItemContext.ofPlayerHand(player, hand));
        if (target == null || held == null) {
            return false;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            if (StorageUtil.move(held, target, variant -> true, Long.MAX_VALUE, transaction) > 0) {
                transaction.commit();
                return true;
            }
        }
        return false;
        *///?}
    }
}
