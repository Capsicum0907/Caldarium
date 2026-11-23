package io.github.capsicum0907.caldarium.fabric.items;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotItemHandler extends Slot {
    private static final Container EMPTY = new SimpleContainer(0);

    private final IItemHandler handler;
    private final int index;

    public SlotItemHandler(IItemHandler handler, int index, int x, int y) {
        super(EMPTY, index, x, y);
        this.handler = handler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && handler.isItemValid(index, stack);
    }

    @Override
    public ItemStack getItem() {
        return handler.getStackInSlot(index);
    }

    @Override
    public void set(ItemStack stack) {
        ((ItemStackHandler) handler).setStackInSlot(index, stack);
        setChanged();
    }

    //? if >=1.19 && <1.20 {
    /*@Override
    public void initialize(ItemStack stack) {
        set(stack);
    }
    *///?}

    @Override
    public void setChanged() {
    }

    @Override
    public int getMaxStackSize() {
        return handler.getSlotLimit(index);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(handler.getSlotLimit(index), stack.getMaxStackSize());
    }

    @Override
    public boolean mayPickup(Player player) {
        return !handler.extractItem(index, 1, true).isEmpty();
    }

    @Override
    public ItemStack remove(int amount) {
        return handler.extractItem(index, amount, false);
    }
}
