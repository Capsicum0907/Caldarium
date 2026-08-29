package io.github.capsicum0907.caldarium;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Putting energy into what is held in a machine.
 *
 * <p>The same interface as everything else here: an item that speaks Forge Energy is
 * charged, whatever mod added it, and one that does not is left alone. There is no
 * list of chargeable things and no way for one to be missing from it.
 */
public final class Charging {
    private Charging() {
    }

    /** Fills each slot in turn, spending no more than the rate across all of them. */
    public static void tick(IItemHandler slots, Store store, int rate) {
        int left = Math.min(rate, store.getEnergyStored());
        for (int slot = 0; slot < slots.getSlots() && left > 0; slot++) {
            IEnergyStorage container = held(slots.getStackInSlot(slot));
            if (container == null || !container.canReceive()) {
                continue;
            }
            int taken = container.receiveEnergy(left, false);
            if (taken > 0) {
                store.spend(taken);
                left -= taken;
            }
        }
    }

    /** What an item can hold, or null when it holds nothing at all. */
    public static IEnergyStorage held(ItemStack stack) {
        return stack.isEmpty() ? null : stack.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    /**
     * Whether a machine is done with it. Something with nothing left to take is
     * finished; something that was never chargeable was put in by mistake and should
     * be able to come back out.
     */
    public static boolean finished(ItemStack stack) {
        IEnergyStorage container = held(stack);
        return container == null || container.getEnergyStored() >= container.getMaxEnergyStored();
    }
}
