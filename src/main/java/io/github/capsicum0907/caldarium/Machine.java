package io.github.capsicum0907.caldarium;

import net.minecraft.world.level.Level;
import net.minecraft.world.Containers;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * What a screen needs to know about a machine before it has seen one.
 *
 * <p>The client builds its own copy of the menu from a buffer, without the block
 * entity, so these two answers travel in it. Everything else the screen shows
 * arrives afterwards through the data slots.
 */
public interface Machine {
    /** Whether there is fuel burning, and so a flame to draw. */
    boolean burns();

    /** The machine's own slots \u2014 fuel, or things being charged. */
    IItemHandler machineSlots();

    /** Whether experience can be poured into it, and so whether the screen offers to. */
    default boolean pours() {
        return false;
    }

    static void spill(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof Machine machine)) {
            return;
        }
        IItemHandler slots = machine.machineSlots();
        for (int slot = 0; slot < slots.getSlots(); slot++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                    slots.getStackInSlot(slot).copy());
        }
    }
}
