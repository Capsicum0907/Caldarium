package io.github.capsicum0907.caldarium;

import net.minecraft.world.item.ItemStack;
//? if neoforge {
import net.neoforged.neoforge.capabilities.Capabilities;
//?} else if forge
//import net.minecraftforge.common.capabilities.ForgeCapabilities;
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
            IEnergyStorage container = held(slots, slot);
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

    private static IEnergyStorage held(IItemHandler slots, int slot) {
        //? if fabric {
        /*return io.github.capsicum0907.caldarium.fabric.Energies.held(slots, slot);
        *///?} else
        return held(slots.getStackInSlot(slot));
    }

    /** What an item can hold, or null when it holds nothing at all. */
    public static IEnergyStorage held(ItemStack stack) {
        //? if neoforge {
        return stack.isEmpty() ? null : stack.getCapability(Capabilities.EnergyStorage.ITEM);
        //?} else if forge {
        /*return stack.isEmpty() ? null : stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        *///?} else
        //return stack.isEmpty() ? null : io.github.capsicum0907.caldarium.fabric.Energies.held(stack);
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
