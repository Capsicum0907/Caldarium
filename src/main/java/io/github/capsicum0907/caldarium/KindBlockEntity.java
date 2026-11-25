package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
//? if >=1.20.5
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
//? if forge {
/*import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
*///?}

/**
 * Everything sized by {@link Tier}: a battery, which holds energy and hands it on,
 * and a charger, which holds energy and puts it into what is in its slots.
 *
 * <p>One class, because the difference between them is two answers the {@link Kind}
 * already gives — whether it pushes, and how many slots it has. Two classes would be
 * the same file twice, free to drift apart.
 */
public class KindBlockEntity extends BlockEntity implements MenuProvider, Machine
        //? if <1.17
        /*, net.minecraft.world.level.block.entity.TickableBlockEntity*/
{
    private Kind kind;
    private Tier tier;
    private CaldariumConfig.Rates rates;
    private Store store;
    private final Neighbours sides = new Neighbours();
    private ItemStackHandler items;
    private MachineData data;

    //? if >=1.17 {
    public KindBlockEntity(BlockPos pos, BlockState state) {
        super(CaldariumRegistry.KIND_ENTITY.get(), pos, state);
        bind((KindBlock) state.getBlock());
    }
    //?} else {
    /*public KindBlockEntity() {
        super(CaldariumRegistry.KIND_ENTITY.get());
    }

    public KindBlockEntity(KindBlock block) {
        this();
        bind(block);
    }
    *///?}

    private void bind(KindBlock block) {
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

    //? if <1.17 {
    /*@Override
    public void tick() {
        if (level != null && !level.isClientSide()) {
            serverTick(level, worldPosition, getBlockState(), this);
        }
    }
    *///?}

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            KindBlockEntity machine) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Charging.tick(machine.items, machine.store, machine.rates.transfer().get());
        // Which way a door points. Read off the block rather than kept here: the shape
        // is where it is decided, and one copy of a fact is enough.
        Direction aimed = machine.kind.door() ? CarrierBlock.aimed(state) : null;
        switch (machine.kind.wiring()) {
            case OPEN -> Pushing.push(machine.sides, server, pos, machine.store);
            case CABLE -> Pushing.along(machine.sides, server, pos, machine.store);
            case INTAKE -> {
                Pulling.pull(machine.sides, server, pos, machine.store, aimed);
                Pushing.feed(machine.sides, server, pos, machine.store, aimed);
            }
            case OUTLET -> {
                Pulling.draw(machine.sides, server, pos, machine.store, aimed);
                Pushing.give(machine.sides, server, pos, machine.store, aimed);
            }
        }
    }

    //? if forge {
    /*private final LazyOptional<Store> energyCapability = LazyOptional.of(this::store);
    private final LazyOptional<ItemStackHandler> itemCapability = LazyOptional.of(this::machineSlots);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            return reachable(side) ? energyCapability.cast() : LazyOptional.empty();
        }
        if (capability == ForgeCapabilities.ITEM_HANDLER && items() != null) {
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
        itemCapability.invalidate();
    }
    *///?}

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMenu(id, inventory, ContainerLevelAccess.create(level, worldPosition),
                false, items, false, data);
    }

    //? if >=1.20.5 {
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    //?} else if >=1.18 {
    /*@Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
    *///?} else {
    /*@Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
    *///?}
        tag.putInt("Energy", store.raw());
        tag.put("Items", items.serializeNBT(registries));
        //? if <1.18
        /*return tag;*/
    }

    //? if >=1.20.5 {
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    //?} else if >=1.18 {
    /*@Override
    public void load(CompoundTag tag) {
        super.load(tag);
    *///?} else {
    /*@Override
    public void load(BlockState state, CompoundTag tag) {
        if (kind == null) {
            bind((KindBlock) state.getBlock());
        }
        super.load(state, tag);
    *///?}
        store.restore(tag.getInt("Energy"));
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
    }
}
