package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Burns what its row accepts and turns it into Forge Energy.
 *
 * <p>The fuel slot is offered as an item handler, which is the whole of the input
 * side: a hopper, a dropper and every mod's pipes ask a block for exactly that and
 * for nothing else, so a generator can be fed before it has a screen of its own.
 */
public class GeneratorBlockEntity extends BlockEntity implements MenuProvider {
    private final Generator row;
    private final CaldariumConfig.Rates rates;
    private final Store store;
    private final Pushing pushing = new Pushing();
    private final ItemStackHandler fuel;

    private final MachineData data;

    private int burning;
    private int burnLength;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(CaldariumRegistry.GENERATOR_ENTITY.get(), pos, state);
        this.row = ((GeneratorBlock) state.getBlock()).row();
        this.rates = CaldariumConfig.GENERATORS.get(row);
        this.store = new Store(Store.Role.SOURCE,
                () -> rates.capacity().get(), () -> rates.transfer().get(), this::setChanged);
        this.data = new MachineData(store::getEnergyStored, store::getMaxEnergyStored,
                () -> burning, () -> burnLength);
        this.fuel = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return row.fuel().accepts(stack);
            }

            /**
             * ⚠ Fuel goes in and does not come back out. Without this a hopper set
             * under the burner — the arrangement everybody builds under a furnace —
             * pulls the coal straight back out of it, and the generator never runs.
             *
             * <p>What is <em>not</em> fuel may still be taken, which is how the empty
             * bucket a lava bucket leaves behind gets collected. Refusing everything
             * would trap it in the slot with nothing able to reach it.
             *
             * <p>The rule asks the item rather than the side the request came from:
             * this mod does not give its faces different opinions.
             */
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return row.fuel().accepts(getStackInSlot(slot))
                        ? ItemStack.EMPTY
                        : super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public Store store() {
        return store;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMenu(id, inventory,
                ContainerLevelAccess.create(level, worldPosition), true, fuel, data);
    }

    public ItemStackHandler fuel() {
        return fuel;
    }

    /** How far through the current piece of fuel it is, as a fraction, for a gauge. */
    public float burnedFraction() {
        return burnLength <= 0 ? 0.0F : (float) burning / burnLength;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            GeneratorBlockEntity generator) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        boolean wasLit = generator.burning > 0;

        // Burning continues even once full, and the surplus is lost. The alternative
        // is a generator that stops mid-log and resumes, which means remembering a
        // fraction of a piece of fuel; a furnace does not do that either.
        if (generator.burning > 0) {
            generator.burning--;
            generator.store.generate(generator.rates.perTick().get());
        }
        if (generator.burning <= 0 && !generator.store.isFull()) {
            generator.light();
        }
        generator.pushing.push(server, pos, generator.store);

        boolean lit = generator.burning > 0;
        if (wasLit != lit) {
            level.setBlock(pos, state.setValue(GeneratorBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    /**
     * Takes one piece of fuel and starts it. The remainder is put back rather than
     * thrown away: a bucket of lava that burns leaves a bucket, and a generator that
     * ate it would be a worse machine than a furnace.
     */
    private void light() {
        ItemStack stack = fuel.getStackInSlot(0);
        int ticks = row.fuel().burnTicks(stack);
        if (ticks <= 0) {
            return;
        }
        ItemStack remainder = stack.getCraftingRemainingItem();
        stack.shrink(1);
        if (stack.isEmpty() && !remainder.isEmpty()) {
            fuel.setStackInSlot(0, remainder);
        }
        burning = ticks;
        burnLength = ticks;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", store.raw());
        tag.putInt("Burning", burning);
        tag.putInt("BurnLength", burnLength);
        tag.put("Fuel", fuel.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        store.restore(tag.getInt("Energy"));
        burning = tag.getInt("Burning");
        burnLength = tag.getInt("BurnLength");
        if (tag.contains("Fuel")) {
            fuel.deserializeNBT(registries, tag.getCompound("Fuel"));
        }
    }
}
