package io.github.capsicum0907.caldarium.fabric.fluids;

//? if >=1.17 {
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
//?} else {
/*import java.util.Optional;

import io.github.capsicum0907.caldarium.GeneratorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidUtil {
    private static final int BUCKET = 1000;

    private FluidUtil() {
    }

    public static Optional<Fluid> getFluidHandler(ItemStack stack) {
        return stack.getItem() instanceof BucketItem bucket && bucket.content != Fluids.EMPTY
                ? Optional.of(bucket.content) : Optional.empty();
    }

    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos,
            Direction side) {
        Optional<Fluid> fluid = getFluidHandler(player.getItemInHand(hand));
        if (!fluid.isPresent() || !(level.getBlockEntity(pos) instanceof GeneratorBlockEntity generator)
                || generator.tank() == null) {
            return false;
        }
        FluidStack bucketful = new FluidStack(fluid.get(), BUCKET);
        if (generator.tank().fill(bucketful,
                io.github.capsicum0907.caldarium.fabric.fluids.capability.templates.FluidTank.FluidAction.SIMULATE) < BUCKET) {
            return false;
        }
        if (!level.isClientSide()) {
            generator.tank().fill(bucketful,
                    io.github.capsicum0907.caldarium.fabric.fluids.capability.templates.FluidTank.FluidAction.EXECUTE);
            if (!player.abilities.instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
        }
        return true;
    }
}
*///?}
