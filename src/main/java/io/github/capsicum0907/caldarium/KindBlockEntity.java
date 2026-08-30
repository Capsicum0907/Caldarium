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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Everything sized by {@link Tier}: a battery, which holds energy and hands it on,
 * and a charger, which holds energy and puts it into what is in its slots.
 *
 * <p>One class, because the difference between them is two answers the {@link Kind}
 * already gives — whether it pushes, and how many slots it has. Two classes would be
 * the same file twice, free to drift apart.
 */
public class KindBlockEntity extends BlockEntity implements MenuProvider, Machine {
    private final Kind kind;
    private final Tier tier;
    private final CaldariumConfig.Rates rates;
    private final Store store;
    private final Neighbours sides = new Neighbours();
    private final ItemStackHandler items;
    private final MachineData data;

    public KindBlockEntity(BlockPos pos, BlockState state) {
        super(CaldariumRegistry.KIND_ENTITY.get(), pos, state);
        KindBlock block = (KindBlock) state.getBlock();
        this.kind = block.kind();
        this.tier = block.tier();
        this.rates = CaldariumConfig.rates(kind, tier);
        this.store = new Store(kind.role(), kind.wiring(),
                () -> rates.capacity().get(), () -> rates.transfer().get(), this::setChanged);
        this.data = new MachineData(store::getEnergyStored, store::getMaxEnergyStored,
                () -> 0, () -> 0, () -> 0, () -> 0);
        this.items = new ItemStackHandler(kind.slots(tier)) {
            /** Only things that can be charged, so a slot cannot be used as a shelf. */
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return Charging.held(stack) != null;
            }

            /**
             * ⚠ Only what the machine is done with. A hopper under a charger should be
             * able to take the finished tools away, and must not be able to take one
             * out again the moment it is put in — which is what an unguarded handler
             * does, and it charges nothing for as long as it goes on.
             */
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return Charging.finished(getStackInSlot(slot))
                        ? super.extractItem(slot, amount, simulate)
                        : ItemStack.EMPTY;
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

    public ItemStackHandler items() {
        return items;
    }

    public Kind kind() {
        return kind;
    }

    /** Nothing here burns. A kind that did would be a row of {@link Generator}. */
    @Override
    public boolean burns() {
        return false;
    }

    @Override
    public ItemStackHandler machineSlots() {
        return items;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            KindBlockEntity machine) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Charging.tick(machine.items, machine.store, machine.rates.transfer().get());
        // Drawn in before it is handed on, so what arrives this tick leaves this
        // tick: an importer that pushed first would always be one tick behind.
        if (machine.kind.pulls()) {
            Pulling.pull(machine.sides, server, pos, machine.store);
        }
        if (machine.kind.pushes()) {
            Pushing.push(machine.sides, server, pos, machine.store);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMenu(id, inventory, ContainerLevelAccess.create(level, worldPosition),
                false, items, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", store.raw());
        tag.put("Items", items.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        store.restore(tag.getInt("Energy"));
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
    }
}
