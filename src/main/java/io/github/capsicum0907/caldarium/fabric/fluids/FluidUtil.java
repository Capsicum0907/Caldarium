package io.github.capsicum0907.caldarium.fabric.fluids;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
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
        return target != null && FluidStorageUtil.interactWithFluidStorage(target, player, hand);
    }
}
