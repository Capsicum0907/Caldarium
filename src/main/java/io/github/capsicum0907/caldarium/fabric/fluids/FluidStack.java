package io.github.capsicum0907.caldarium.fabric.fluids;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidStack {
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    private final Fluid fluid;
    private int amount;

    public FluidStack(Fluid fluid, int amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public Fluid getFluid() {
        return isEmpty() ? Fluids.EMPTY : fluid;
    }

    public int getAmount() {
        return isEmpty() ? 0 : amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void grow(int by) {
        amount += by;
    }

    public void shrink(int by) {
        amount -= by;
    }

    public boolean isEmpty() {
        return this == EMPTY || fluid == Fluids.EMPTY || amount <= 0;
    }

    public FluidStack copy() {
        return isEmpty() ? EMPTY : new FluidStack(fluid, amount);
    }

    public FluidStack copyWithAmount(int amount) {
        return isEmpty() || amount <= 0 ? EMPTY : new FluidStack(fluid, amount);
    }

    public boolean isSameFluid(FluidStack other) {
        return getFluid() == other.getFluid();
    }
}
