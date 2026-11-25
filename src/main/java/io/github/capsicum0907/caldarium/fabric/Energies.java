package io.github.capsicum0907.caldarium.fabric;

//? if >=1.17 {
import io.github.capsicum0907.caldarium.Store;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import team.reborn.energy.api.EnergyStorage;

public final class Energies {
    private Energies() {
    }

    public static EnergyStorage of(Store store) {
        return store == null ? null : new Exposed(store);
    }

    public static io.github.capsicum0907.caldarium.fabric.energy.IEnergyStorage find(Level level, BlockPos pos,
            Direction side) {
        return wrap(EnergyStorage.SIDED.find(level, pos, side));
    }

    public static io.github.capsicum0907.caldarium.fabric.energy.IEnergyStorage held(ItemStack stack) {
        return wrap(EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack)));
    }

    public static io.github.capsicum0907.caldarium.fabric.energy.IEnergyStorage held(
            io.github.capsicum0907.caldarium.fabric.items.IItemHandler slots, int slot) {
        if (slots.getStackInSlot(slot).isEmpty()) {
            return null;
        }
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(Transfer.slot(slots, slot));
        return wrap(context.find(EnergyStorage.ITEM));
    }

    private static io.github.capsicum0907.caldarium.fabric.energy.IEnergyStorage wrap(EnergyStorage found) {
        if (found == null) {
            return null;
        }
        if (found instanceof Exposed ours) {
            return ours.store;
        }
        return new Foreign(found);
    }

    private static final class Exposed extends SnapshotParticipant<Integer> implements EnergyStorage {
        private final Store store;

        private Exposed(Store store) {
            this.store = store;
        }

        @Override
        public boolean supportsInsertion() {
            return store.canReceive();
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            int offered = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            int taken = store.receiveEnergy(offered, true);
            if (taken > 0) {
                updateSnapshots(transaction);
                store.receiveEnergy(taken, false);
            }
            return taken;
        }

        @Override
        public boolean supportsExtraction() {
            return store.canExtract();
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            int wanted = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            int given = store.extractEnergy(wanted, true);
            if (given > 0) {
                updateSnapshots(transaction);
                store.extractEnergy(given, false);
            }
            return given;
        }

        @Override
        public long getAmount() {
            return store.getEnergyStored();
        }

        @Override
        public long getCapacity() {
            return store.getMaxEnergyStored();
        }

        @Override
        protected Integer createSnapshot() {
            return store.raw();
        }

        @Override
        protected void readSnapshot(Integer snapshot) {
            store.restore(snapshot);
        }
    }

    private static final class Foreign implements io.github.capsicum0907.caldarium.fabric.energy.IEnergyStorage {
        private final EnergyStorage storage;

        private Foreign(EnergyStorage storage) {
            this.storage = storage;
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            if (toReceive <= 0 || !storage.supportsInsertion() || Transaction.isOpen()) {
                return 0;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                long taken = storage.insert(toReceive, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return (int) taken;
            }
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            if (toExtract <= 0 || !storage.supportsExtraction() || Transaction.isOpen()) {
                return 0;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                long given = storage.extract(toExtract, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return (int) given;
            }
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(storage.getAmount(), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(storage.getCapacity(), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return storage.supportsExtraction();
        }

        @Override
        public boolean canReceive() {
            return storage.supportsInsertion();
        }
    }
}
//?} else {
/*import io.github.capsicum0907.caldarium.GeneratorBlockEntity;
import io.github.capsicum0907.caldarium.KindBlockEntity;
import io.github.capsicum0907.caldarium.Store;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import team.reborn.energy.Energy;
import team.reborn.energy.EnergyHandler;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyStorage;
import team.reborn.energy.EnergyTier;

public final class Energies {
    private Energies() {
    }

    public static void register() {
        Energy.registerHolder(entity -> entity instanceof GeneratorBlockEntity || entity instanceof KindBlockEntity,
                Energies::holder);
    }

    private static EnergyStorage holder(Object entity) {
        if (entity instanceof GeneratorBlockEntity generator) {
            return new Exposed(generator.store(), null);
        }
        return new Exposed(((KindBlockEntity) entity).store(), (KindBlockEntity) entity);
    }

    public static io.github.capsicum0907.caldarium.fabric.energy.IEnergyStorage find(Level level, BlockPos pos,
            Direction side) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof GeneratorBlockEntity generator) {
            return generator.store();
        }
        if (entity instanceof KindBlockEntity machine) {
            return machine.reachable(side) ? machine.store() : null;
        }
        return entity != null && Energy.valid(entity) ? new Foreign(entity, side(side)) : null;
    }

    public static io.github.capsicum0907.caldarium.fabric.energy.IEnergyStorage held(ItemStack stack) {
        return Energy.valid(stack) ? new Foreign(stack, EnergySide.UNKNOWN) : null;
    }

    public static io.github.capsicum0907.caldarium.fabric.energy.IEnergyStorage held(
            io.github.capsicum0907.caldarium.fabric.items.IItemHandler slots, int slot) {
        return held(slots.getStackInSlot(slot));
    }

    private static EnergySide side(Direction side) {
        return side == null ? EnergySide.UNKNOWN : EnergySide.fromMinecraft(side);
    }

    private static Direction direction(EnergySide side) {
        return side == EnergySide.UNKNOWN ? null : Direction.values()[side.ordinal()];
    }

    private static final class Exposed implements EnergyStorage {
        private final Store store;
        private final KindBlockEntity doors;

        private Exposed(Store store, KindBlockEntity doors) {
            this.store = store;
            this.doors = doors;
        }

        private boolean open(EnergySide side) {
            return doors == null || doors.reachable(direction(side));
        }

        @Override
        public double getStored(EnergySide side) {
            return open(side) ? store.getEnergyStored() : 0.0;
        }

        @Override
        public void setStored(double amount) {
            int target = (int) Math.max(0.0, Math.min(Integer.MAX_VALUE, amount));
            int stored = store.raw();
            if (target > stored) {
                store.fill(target - stored);
            } else if (target < stored) {
                store.spend(stored - target);
            }
        }

        @Override
        public double getMaxStoredPower() {
            return store.getMaxEnergyStored();
        }

        @Override
        public double getMaxInput(EnergySide side) {
            return open(side) && store.canReceive() ? store.transferRate() : 0.0;
        }

        @Override
        public double getMaxOutput(EnergySide side) {
            return open(side) && store.canExtract() ? store.transferRate() : 0.0;
        }

        @Override
        public EnergyTier getTier() {
            return EnergyTier.INFINITE;
        }
    }

    private static final class Foreign implements io.github.capsicum0907.caldarium.fabric.energy.IEnergyStorage {
        private final Object target;
        private final EnergySide side;

        private Foreign(Object target, EnergySide side) {
            this.target = target;
            this.side = side;
        }

        private EnergyHandler handler() {
            return Energy.of(target).side(side);
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            EnergyHandler handler = handler();
            return (int) (simulate ? handler.simulate() : handler).insert(toReceive);
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            EnergyHandler handler = handler();
            return (int) (simulate ? handler.simulate() : handler).extract(toExtract);
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(handler().getEnergy(), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(handler().getMaxStored(), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return handler().getMaxOutput() > 0.0;
        }

        @Override
        public boolean canReceive() {
            return handler().getMaxInput() > 0.0;
        }
    }
}
*///?}
