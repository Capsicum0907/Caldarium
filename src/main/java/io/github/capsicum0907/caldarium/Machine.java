package io.github.capsicum0907.caldarium;

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
}
