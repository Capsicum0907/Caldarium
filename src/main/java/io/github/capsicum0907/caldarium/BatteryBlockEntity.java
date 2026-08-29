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
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds energy and hands it on. A row of these is the line that carries — see the
 * asymmetric rule in {@link Pushing}, which is what keeps two of them from pushing
 * into each other forever.
 */
public class BatteryBlockEntity extends BlockEntity implements MenuProvider {
    private final Tier tier;
    private final Store store;
    private final Pushing pushing = new Pushing();
    private final MachineData data;

    /** Nothing goes in a battery yet. The slot arrives with charging. */
    private static final ItemStackHandler NOTHING = new ItemStackHandler(0);

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(CaldariumRegistry.BATTERY_ENTITY.get(), pos, state);
        this.tier = ((BatteryBlock) state.getBlock()).tier();
        CaldariumConfig.Rates rates = CaldariumConfig.BATTERIES.get(tier);
        this.store = new Store(Store.Role.BUFFER,
                () -> rates.capacity().get(), () -> rates.transfer().get(), this::setChanged);
        this.data = new MachineData(store::getEnergyStored, store::getMaxEnergyStored,
                () -> 0, () -> 0);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMenu(id, inventory,
                ContainerLevelAccess.create(level, worldPosition), false, NOTHING, data);
    }

    public Store store() {
        return store;
    }

    public Tier tier() {
        return tier;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            BatteryBlockEntity battery) {
        if (level instanceof ServerLevel server) {
            battery.pushing.push(server, pos, battery.store);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", store.raw());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        store.restore(tag.getInt("Energy"));
    }
}
