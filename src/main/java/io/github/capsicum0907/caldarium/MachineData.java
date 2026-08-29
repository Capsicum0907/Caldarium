package io.github.capsicum0907.caldarium;

import java.util.function.IntSupplier;

import net.minecraft.world.inventory.ContainerData;

/**
 * The four numbers a screen shows, in the only shape the game will carry them.
 *
 * <p>⚠ <b>A data slot is sixteen bits on the wire.</b> {@code
 * ClientboundContainerSetDataPacket} writes its value with {@code writeShort}, so a
 * battery holding 400,000 FE would arrive at the screen as 13,568. Every number here
 * is therefore sent as two slots, the top half and the bottom half, and put back
 * together on the other side by {@link #whole}.
 *
 * <p>The values are read from suppliers rather than copied, so the server side is
 * always the block entity's own state; the client side is a
 * {@code SimpleContainerData} that the game fills in.
 */
public final class MachineData implements ContainerData {
    public static final int ENERGY = 0;
    public static final int CAPACITY = 1;
    public static final int BURNING = 2;
    public static final int BURN_LENGTH = 3;

    /** Two slots each. */
    public static final int SIZE = 8;

    private final IntSupplier[] values;

    public MachineData(IntSupplier energy, IntSupplier capacity, IntSupplier burning,
            IntSupplier burnLength) {
        this.values = new IntSupplier[] { energy, capacity, burning, burnLength };
    }

    @Override
    public int get(int index) {
        int whole = values[index >> 1].getAsInt();
        return (index & 1) == 0 ? whole >>> 16 : whole & 0xFFFF;
    }

    /**
     * Never called on the server: these are read from the block entity, and the game
     * only ever writes into the copy the client keeps.
     */
    @Override
    public void set(int index, int value) {
    }

    @Override
    public int getCount() {
        return SIZE;
    }

    /**
     * The two halves, made one again. Both are masked because {@code readShort} sign
     * extends, which would turn a bottom half above 32,767 into a negative number.
     */
    public static int whole(ContainerData data, int which) {
        return (data.get(which * 2) & 0xFFFF) << 16 | (data.get(which * 2 + 1) & 0xFFFF);
    }
}
