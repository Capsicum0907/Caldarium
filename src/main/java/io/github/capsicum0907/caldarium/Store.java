package io.github.capsicum0907.caldarium;

import java.util.function.IntSupplier;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * The energy a block holds. One class for every block here, told apart by
 * {@link Role} and by {@link Wiring}.
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
        /**
         * Where energy comes into the mod, and never a place to put any: something
         * that took a push would be a battery. What it is made from is not this
         * enum's business — a generator burns for it, an importer draws it out of
         * somebody else's machine, and to everything downstream they are the same.
         */
        SOURCE,
        /** Holds energy on its way somewhere. */
        BUFFER,
        /**
         * Spends energy on something other than passing it on.
         *
         * <p>⚠ This exists because of the downhill rule in {@link Pushing}, which only
         * holds between two buffers. A charger counted as a buffer would be starved by
         * the battery beside it the moment it was the fuller of the two by share — the
         * same trap as a running machine from another mod, and for the same reason.
         */
        SINK
    }

    private final Role role;
    private final Wiring wiring;
    private final IntSupplier capacity;
    private final IntSupplier transfer;
    private final Runnable changed;

    private int stored;

    public Store(Role role, Wiring wiring, IntSupplier capacity, IntSupplier transfer,
            Runnable changed) {
        this.role = role;
        this.wiring = wiring;
        this.capacity = capacity;
        this.transfer = transfer;
        this.changed = changed;
    }

    public Role role() {
        return role;
    }

    /** Which side of the boundary between the mods it will offer what it holds. */
    public Wiring wiring() {
        return wiring;
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
        return role != Role.SOURCE;
    }

    /** Nothing comes back out of a sink: what went in was spent on what it holds. */
    @Override
    public boolean canExtract() {
        return role != Role.SINK;
    }

    /**
     * ⚠ Arithmetic on {@link #stored}, never on {@link #getEnergyStored()}.
     *
     * <p>The two differ in exactly one situation and it matters: capacity is a live
     * setting, so lowering it leaves blocks holding more than they now can. Writing
     * {@code stored = getEnergyStored() + taken} would quietly throw the surplus away
     * on the next transfer of a single unit. Working from the raw amount instead, the
     * excess is not lost — it drains out normally as the block is used, and the block
     * simply refuses to take more until it is back under the new limit.
     */
    @Override
    public int receiveEnergy(int offered, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }
        int room = getMaxEnergyStored() - stored;
        int taken = Math.min(Math.min(offered, transferRate()), Math.max(0, room));
        if (taken > 0 && !simulate) {
            stored += taken;
            changed.run();
        }
        return Math.max(0, taken);
    }

    @Override
    public int extractEnergy(int wanted, boolean simulate) {
        if (!canExtract()) {
            return 0;
        }
        int given = Math.min(Math.min(wanted, transferRate()), stored);
        if (given > 0 && !simulate) {
            stored -= given;
            changed.run();
        }
        return Math.max(0, given);
    }

    /**
     * What a block puts into itself — burnt out of fuel, gathered off the sky, or
     * drawn out of somebody else's machine. Goes around {@link #canReceive}, which
     * exists to keep <em>other</em> blocks from filling a source, not to stop one
     * working.
     */
    public int fill(int amount) {
        int made = Math.min(amount, room());
        if (made > 0) {
            stored += made;
            changed.run();
        }
        return Math.max(0, made);
    }

    /**
     * How much more would fit. ⚠ Measured from the raw amount, so a capacity lowered
     * under a full block reads as no room at all rather than as room to spare.
     */
    public int room() {
        return Math.max(0, getMaxEnergyStored() - stored);
    }

    /**
     * What a machine spends on itself. The counterpart of {@link #fill}: it goes
     * around {@link #canExtract}, which is there to stop <em>other</em> blocks
     * draining a sink rather than to stop the sink doing its job.
     */
    public int spend(int amount) {
        int spent = Math.min(amount, stored);
        if (spent > 0) {
            stored -= spent;
            changed.run();
        }
        return Math.max(0, spent);
    }

    public boolean isEmpty() {
        return getEnergyStored() <= 0;
    }

    public boolean isFull() {
        return stored >= getMaxEnergyStored();
    }

    /** Saving and loading. The stored amount is the whole of the state. */
    public int raw() {
        return stored;
    }

    public void restore(int amount) {
        stored = Math.max(0, amount);
    }
}
