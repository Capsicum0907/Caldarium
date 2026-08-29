package io.github.capsicum0907.caldarium;

import java.util.function.IntSupplier;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * The energy a block holds. One class for both blocks, told apart by {@link Role}.
 *
 * <p>This is also the object handed out as the capability, which is what makes
 * {@link Pushing} able to recognise its own kind without a second lookup: a
 * neighbour that answers with a {@code Store} is one of ours.
 *
 * <p>Limits are suppliers rather than numbers, because they come from the config and
 * the config is reloadable. A block that read its capacity once at placement would
 * keep it after the setting changed.
 */
public final class Store implements IEnergyStorage {
    /** What the block is for. The push rule in {@link Pushing} turns on this. */
    public enum Role {
        /** Makes energy. Never accepts any: something that did would be a battery. */
        SOURCE,
        /** Holds energy on its way somewhere. */
        BUFFER
    }

    private final Role role;
    private final IntSupplier capacity;
    private final IntSupplier transfer;
    private final Runnable changed;

    private int stored;

    public Store(Role role, IntSupplier capacity, IntSupplier transfer, Runnable changed) {
        this.role = role;
        this.capacity = capacity;
        this.transfer = transfer;
        this.changed = changed;
    }

    public Role role() {
        return role;
    }

    @Override
    public int getEnergyStored() {
        return Math.min(stored, getMaxEnergyStored());
    }

    @Override
    public int getMaxEnergyStored() {
        return Math.max(0, capacity.getAsInt());
    }

    /** How much may cross the boundary in one call, in either direction. */
    public int transferRate() {
        return Math.max(0, transfer.getAsInt());
    }

    @Override
    public boolean canReceive() {
        return role == Role.BUFFER;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public int receiveEnergy(int offered, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }
        int taken = Math.min(Math.min(offered, transferRate()), getMaxEnergyStored() - getEnergyStored());
        if (taken > 0 && !simulate) {
            stored = getEnergyStored() + taken;
            changed.run();
        }
        return Math.max(0, taken);
    }

    @Override
    public int extractEnergy(int wanted, boolean simulate) {
        int given = Math.min(Math.min(wanted, transferRate()), getEnergyStored());
        if (given > 0 && !simulate) {
            stored = getEnergyStored() - given;
            changed.run();
        }
        return Math.max(0, given);
    }

    /**
     * What a generator does to itself. Goes around {@link #canReceive}, which exists
     * to keep <em>other</em> blocks from filling a generator, not to stop it working.
     */
    public int generate(int amount) {
        int made = Math.min(amount, getMaxEnergyStored() - getEnergyStored());
        if (made > 0) {
            stored = getEnergyStored() + made;
            changed.run();
        }
        return Math.max(0, made);
    }

    public boolean isEmpty() {
        return getEnergyStored() <= 0;
    }

    public boolean isFull() {
        return getEnergyStored() >= getMaxEnergyStored();
    }

    /** Saving and loading. The stored amount is the whole of the state. */
    public int raw() {
        return stored;
    }

    public void restore(int amount) {
        stored = Math.max(0, amount);
    }
}
