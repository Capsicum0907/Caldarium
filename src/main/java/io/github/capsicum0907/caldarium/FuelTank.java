package io.github.capsicum0907.caldarium;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * The tank a generator burns out of. It takes fuel in and never gives it back.
 *
 * <p>Only what burns goes in, which {@link FluidTank#fill} checks for us: it asks
 * {@code isFluidValid} before anything else, so a pipe carrying water is refused the
 * same way a hand carrying it is.
 *
 * <p>⚠ <b>And nothing comes out.</b> A pipe set to extract, sat against a generator,
 * would otherwise pull the fuel straight back out of it — the same trap as a hopper
 * under a furnace, and one a player reads as the machine being broken rather than as
 * their own pipe doing exactly what they told it to. The cost is that fuel put in by
 * mistake stays in until the block is broken; the alternative costs a working
 * machine, which is worse.
 *
 * <p>{@link #spend} is how the generator itself takes a draught, the same way
 * {@link Store#spend} is how a machine spends energy on its own work.
 */
public class FuelTank extends FluidTank {
    private final Runnable changed;

    public FuelTank(int capacity, Runnable changed) {
        super(capacity, held -> Fuel.burnTicks(held.getFluid()) > 0);
        this.changed = changed;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    /** What the machine takes for itself, going around the refusal above. */
    public FluidStack spend(int amount) {
        return super.drain(amount, FluidAction.EXECUTE);
    }

    @Override
    protected void onContentsChanged() {
        changed.run();
    }
}
