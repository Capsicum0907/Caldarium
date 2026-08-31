package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    /**
     * ⚠ Null where there is nothing to put anything in, which is what the generator
     * already answers for its fuel slot. An empty handler is not the same as none: a
     * hopper lines itself up against anything that answers at all, and then spends the
     * rest of its life pushing into a block with no slots.
     *
     * <p>⚠ Asked here rather than by whoever hands the capability out. The same
     * question asked in two places is one place too many — the second copy read this
     * one's null and crashed on it, which is how it came to be written down.
     */
    public ItemStackHandler items() {
        return items.getSlots() == 0 ? null : items;
    }

    public Kind kind() {
        return kind;
    }

    /**
     * Whether energy can be got at from this side at all.
     *
     * <p>⚠ Not on the face a door is aimed at. That face is the end of the line, and
     * anything that could read the block there would join to it from that side — a
     * cable would grow an arm into the back of a drill and push through it, which is
     * the line carrying on past its own end.
     *
     * <p>The block still reaches <em>out</em> through that face, because reaching out
     * is asking the neighbour rather than answering it.
     */
    public boolean reachable(Direction side) {
        return side == null || !kind.door()
                || getBlockState().getValue(CarrierBlock.JOINTS.get(side)) != Joint.AIMED;
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
        // Which way a door points. Read off the block rather than kept here: the shape
        // is where it is decided, and one copy of a fact is enough.
        Direction aimed = machine.kind.door() ? CarrierBlock.aimed(state) : null;
        // Drawn in before it is handed on, so what arrives this tick leaves this
        // tick: an importer that pushed first would always be one tick behind.
        // ⭐ The aim goes to whichever half of it the kind uses, and to neither for a
        // cable. That one face is the only thing telling the three of them apart.
        if (machine.kind.pulls()) {
            Pulling.pull(machine.sides, server, pos, machine.store, aimed);
        }
        if (machine.kind.pushes()) {
            Pushing.push(machine.sides, server, pos, machine.store, aimed,
                    machine.kind.gives());
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
