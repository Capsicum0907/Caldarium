package io.github.capsicum0907.caldarium.fabric.fluids.capability.templates;

import java.util.function.Predicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
//? if >=1.20.5
import net.minecraft.core.HolderLookup;
//? if >=1.19.3 {
import net.minecraft.core.registries.BuiltInRegistries;
//?}

public class FluidTank {
    public enum FluidAction {
        EXECUTE,
        SIMULATE;

        public boolean execute() {
            return this == EXECUTE;
        }

        public boolean simulate() {
            return this == SIMULATE;
        }
    }

    private static final String FLUID = "FluidName";
    private static final String AMOUNT = "Amount";

    protected final Predicate<io.github.capsicum0907.caldarium.fabric.fluids.FluidStack> validator;
    protected io.github.capsicum0907.caldarium.fabric.fluids.FluidStack fluid = io.github.capsicum0907.caldarium.fabric.fluids.FluidStack.EMPTY;
    protected int capacity;

    public FluidTank(int capacity, Predicate<io.github.capsicum0907.caldarium.fabric.fluids.FluidStack> validator) {
        this.capacity = capacity;
        this.validator = validator;
    }

    public io.github.capsicum0907.caldarium.fabric.fluids.FluidStack getFluid() {
        return fluid;
    }

    public int getFluidAmount() {
        return fluid.getAmount();
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isFluidValid(io.github.capsicum0907.caldarium.fabric.fluids.FluidStack stack) {
        return validator.test(stack);
    }

    public int fill(io.github.capsicum0907.caldarium.fabric.fluids.FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource)) {
            return 0;
        }
        if (action.simulate()) {
            if (fluid.isEmpty()) {
                return Math.min(capacity, resource.getAmount());
            }
            return fluid.isSameFluid(resource) ? Math.min(capacity - fluid.getAmount(), resource.getAmount()) : 0;
        }
        if (fluid.isEmpty()) {
            fluid = resource.copyWithAmount(Math.min(capacity, resource.getAmount()));
            onContentsChanged();
            return fluid.getAmount();
        }
        if (!fluid.isSameFluid(resource)) {
            return 0;
        }
        int filled = Math.min(capacity - fluid.getAmount(), resource.getAmount());
        if (filled > 0) {
            fluid.grow(filled);
            onContentsChanged();
        }
        return filled;
    }

    public io.github.capsicum0907.caldarium.fabric.fluids.FluidStack drain(io.github.capsicum0907.caldarium.fabric.fluids.FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.isSameFluid(fluid)) {
            return io.github.capsicum0907.caldarium.fabric.fluids.FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    public io.github.capsicum0907.caldarium.fabric.fluids.FluidStack drain(int maxDrain, FluidAction action) {
        int drained = Math.min(maxDrain, fluid.getAmount());
        io.github.capsicum0907.caldarium.fabric.fluids.FluidStack stack = fluid.copyWithAmount(drained);
        if (action.execute() && drained > 0) {
            fluid.shrink(drained);
            if (fluid.getAmount() <= 0) {
                fluid = io.github.capsicum0907.caldarium.fabric.fluids.FluidStack.EMPTY;
            }
            onContentsChanged();
        }
        return stack;
    }

    public void setFluid(io.github.capsicum0907.caldarium.fabric.fluids.FluidStack stack) {
        fluid = stack.copy();
    }

    protected void onContentsChanged() {
    }

    //? if >=1.20.5 {
    public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag tag) {
    //?} else
    //public CompoundTag writeToNBT(CompoundTag tag) {
        if (!fluid.isEmpty()) {
            tag.putString(FLUID, fluids().getKey(fluid.getFluid()).toString());
            tag.putInt(AMOUNT, fluid.getAmount());
        }
        return tag;
    }

    //? if >=1.20.5 {
    public FluidTank readFromNBT(HolderLookup.Provider registries, CompoundTag tag) {
    //?} else
    //public FluidTank readFromNBT(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(FLUID));
        Fluid read = id == null ? null : fluids().get(id);
        fluid = read == null ? io.github.capsicum0907.caldarium.fabric.fluids.FluidStack.EMPTY : new io.github.capsicum0907.caldarium.fabric.fluids.FluidStack(read, tag.getInt(AMOUNT));
        return this;
    }

    private static net.minecraft.core.Registry<Fluid> fluids() {
        //? if >=1.19.3 {
        return BuiltInRegistries.FLUID;
        //?} else
        //return net.minecraft.core.Registry.FLUID;
    }
}
