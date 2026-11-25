package io.github.capsicum0907.caldarium.fabric.items;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
//? if >=1.20.5
import net.minecraft.core.HolderLookup;

public class ItemStackHandler implements IItemHandler {
    protected NonNullList<ItemStack> stacks;

    public ItemStackHandler(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }
        ItemStack existing = stacks.get(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!same(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }
        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, copyWithCount(stack, reachedLimit ? limit : stack.getCount()));
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }
        return reachedLimit ? copyWithCount(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            }
            return existing.copy();
        }
        if (!simulate) {
            stacks.set(slot, copyWithCount(existing, existing.getCount() - toExtract));
            onContentsChanged(slot);
        }
        return copyWithCount(existing, toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    protected void onContentsChanged(int slot) {
    }

    //? if >=1.20.5 {
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
    //?} else
    //public CompoundTag serializeNBT() {
        ListTag items = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag item = new CompoundTag();
                item.putInt("Slot", i);
                //? if >=1.20.5 {
                items.add(stacks.get(i).save(registries, item));
                //?} else {
                /*stacks.get(i).save(item);
                items.add(item);
                *///?}
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Items", items);
        tag.putInt("Size", stacks.size());
        return tag;
    }

    //? if >=1.20.5 {
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
    //?} else
    //public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Size")) {
            stacks = NonNullList.withSize(tag.getInt("Size"), ItemStack.EMPTY);
        }
        ListTag items = tag.getList("Items", new CompoundTag().getId());
        for (int i = 0; i < items.size(); i++) {
            CompoundTag item = items.getCompound(i);
            int slot = item.getInt("Slot");
            if (slot >= 0 && slot < stacks.size()) {
                //? if >=1.20.5 {
                stacks.set(slot, ItemStack.parse(registries, item).orElse(ItemStack.EMPTY));
                //?} else
                //stacks.set(slot, ItemStack.of(item));
            }
        }
    }

    private static boolean same(ItemStack a, ItemStack b) {
        //? if >=1.20.5 {
        return ItemStack.isSameItemSameComponents(a, b);
        //?} else if >=1.17 {
        /*return ItemStack.isSameItemSameTags(a, b);
        *///?} else
        /*return ItemStack.isSame(a, b) && ItemStack.tagMatches(a, b);*/
    }

    private static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }
}
