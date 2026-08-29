package io.github.capsicum0907.caldarium;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * What a generator will burn, and for how long.
 *
 * <p>One of these belongs to each row of {@link Generator} rather than being a rule
 * the whole mod shares: a type of generator is, more than anything else, what it
 * accepts. A second type that eats something other than firewood adds a constant
 * here and a row there, and touches nothing else.
 */
public enum Fuel {
    /**
     * Whatever a furnace would burn, for as long as a furnace would burn it.
     *
     * <p>Deliberately not a list of our own. Every mod that adds a burnable thing
     * has already told the game how long it lasts, and asking the item is how that
     * answer is reached; writing the list here would mean the generator quietly
     * refuses fuels that the mod adding them believes it supports.
     */
    FURNACE {
        @Override
        public int burnTicks(ItemStack stack) {
            return stack.getBurnTime(RecipeType.SMELTING);
        }
    };

    /** Zero when this is not fuel at all. */
    public abstract int burnTicks(ItemStack stack);

    /**
     * How long a bucket of something burns, which is the whole of what a fluid fuel
     * is. Asked of the bucket rather than kept in a list here, for the same reason
     * the solid fuels are asked of the item: the mod that added the fluid has
     * already answered, and a list of our own could only disagree with it.
     */
    public static int burnTicks(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return 0;
        }
        Item bucket = fluid.getBucket();
        return bucket == null ? 0 : FURNACE.burnTicks(new ItemStack(bucket));
    }

    public boolean accepts(ItemStack stack) {
        return burnTicks(stack) > 0;
    }
}
