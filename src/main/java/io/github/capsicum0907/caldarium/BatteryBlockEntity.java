package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds energy and hands it on. A row of these is the line that carries — see the
 * asymmetric rule in {@link Pushing}, which is what keeps two of them from pushing
 * into each other forever.
 */
public class BatteryBlockEntity extends BlockEntity {
    private final Tier tier;
    private final Store store;
    private final Pushing pushing = new Pushing();

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(CaldariumRegistry.BATTERY_ENTITY.get(), pos, state);
        this.tier = ((BatteryBlock) state.getBlock()).tier();
        CaldariumConfig.Rates rates = CaldariumConfig.BATTERIES.get(tier);
        this.store = new Store(Store.Role.BUFFER,
                () -> rates.capacity().get(), () -> rates.transfer().get(), this::setChanged);
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
