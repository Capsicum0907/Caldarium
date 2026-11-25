package io.github.capsicum0907.caldarium.fabric;

//? if >=1.17 {
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

public final class Transfer {
    private static final long DROPLETS_PER_MILLIBUCKET = FluidConstants.BUCKET / 1000;

    private Transfer() {
    }

    public static Storage<ItemVariant> items(io.github.capsicum0907.caldarium.fabric.items.IItemHandler handler) {
        if (handler == null) {
            return null;
        }
        List<Slot> slots = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            slots.add(new Slot(handler, slot));
        }
        return new CombinedStorage<>(slots);
    }

    public static SingleSlotStorage<ItemVariant> slot(io.github.capsicum0907.caldarium.fabric.items.IItemHandler handler,
            int slot) {
        return new Slot(handler, slot);
    }

    public static Storage<FluidVariant> fluids(
            io.github.capsicum0907.caldarium.fabric.fluids.capability.templates.FluidTank tank) {
        return tank == null ? null : new Tank(tank);
    }

    private static final class Slot extends SingleStackStorage {
        private final io.github.capsicum0907.caldarium.fabric.items.IItemHandler handler;
        private final int slot;

        private Slot(io.github.capsicum0907.caldarium.fabric.items.IItemHandler handler, int slot) {
            this.handler = handler;
            this.slot = slot;
        }

        @Override
        protected ItemStack getStack() {
            return handler.getStackInSlot(slot);
        }

        @Override
        protected void setStack(ItemStack stack) {
            ((io.github.capsicum0907.caldarium.fabric.items.ItemStackHandler) handler).setStackInSlot(slot, stack);
        }

        @Override
        protected boolean canInsert(ItemVariant variant) {
            return handler.isItemValid(slot, variant.toStack());
        }

        @Override
        protected boolean canExtract(ItemVariant variant) {
            return !handler.extractItem(slot, 1, true).isEmpty();
        }

        @Override
        protected int getCapacity(ItemVariant variant) {
            return Math.min(handler.getSlotLimit(slot), variant.toStack().getMaxStackSize());
        }
    }

    private static final class Tank extends SnapshotParticipant<io.github.capsicum0907.caldarium.fabric.fluids.FluidStack>
            implements SingleSlotStorage<FluidVariant> {
        private final io.github.capsicum0907.caldarium.fabric.fluids.capability.templates.FluidTank tank;

        private Tank(io.github.capsicum0907.caldarium.fabric.fluids.capability.templates.FluidTank tank) {
            this.tank = tank;
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            int millibuckets = (int) Math.min(maxAmount / DROPLETS_PER_MILLIBUCKET, Integer.MAX_VALUE);
            io.github.capsicum0907.caldarium.fabric.fluids.FluidStack offered =
                    new io.github.capsicum0907.caldarium.fabric.fluids.FluidStack(resource.getFluid(), millibuckets);
            int filled = tank.fill(offered,
                    io.github.capsicum0907.caldarium.fabric.fluids.capability.templates.FluidTank.FluidAction.SIMULATE);
            if (filled <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            tank.fill(offered.copyWithAmount(filled),
                    io.github.capsicum0907.caldarium.fabric.fluids.capability.templates.FluidTank.FluidAction.EXECUTE);
            return filled * DROPLETS_PER_MILLIBUCKET;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            int millibuckets = (int) Math.min(maxAmount / DROPLETS_PER_MILLIBUCKET, Integer.MAX_VALUE);
            io.github.capsicum0907.caldarium.fabric.fluids.FluidStack wanted =
                    new io.github.capsicum0907.caldarium.fabric.fluids.FluidStack(resource.getFluid(), millibuckets);
            io.github.capsicum0907.caldarium.fabric.fluids.FluidStack drained = tank.drain(wanted,
                    io.github.capsicum0907.caldarium.fabric.fluids.capability.templates.FluidTank.FluidAction.SIMULATE);
            if (drained.isEmpty()) {
                return 0;
            }
            updateSnapshots(transaction);
            tank.drain(drained,
                    io.github.capsicum0907.caldarium.fabric.fluids.capability.templates.FluidTank.FluidAction.EXECUTE);
            return drained.getAmount() * DROPLETS_PER_MILLIBUCKET;
        }

        @Override
        public boolean isResourceBlank() {
            return tank.getFluid().isEmpty();
        }

        @Override
        public FluidVariant getResource() {
            return tank.getFluid().isEmpty() ? FluidVariant.blank() : FluidVariant.of(tank.getFluid().getFluid());
        }

        @Override
        public long getAmount() {
            return tank.getFluidAmount() * DROPLETS_PER_MILLIBUCKET;
        }

        @Override
        public long getCapacity() {
            return tank.getCapacity() * DROPLETS_PER_MILLIBUCKET;
        }

        @Override
        protected io.github.capsicum0907.caldarium.fabric.fluids.FluidStack createSnapshot() {
            return tank.getFluid().copy();
        }

        @Override
        protected void readSnapshot(io.github.capsicum0907.caldarium.fabric.fluids.FluidStack snapshot) {
            tank.setFluid(snapshot);
        }
    }
}
//?}
