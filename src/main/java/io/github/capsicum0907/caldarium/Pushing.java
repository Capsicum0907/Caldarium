package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * The transport layer, entire: on its tick a block offers what it has to the six
 * blocks touching it. There is no route, no plan and no cable — a cable mod does
 * that job, and this is what makes one unnecessary for two blocks side by side.
 *
 * <p><b>The rule is asymmetric, and that is the whole design.</b>
 *
 * <ul>
 * <li>To another buffer of ours, only downhill — to a neighbour holding a smaller
 *     share of what it can hold. Without this, two touching batteries push into each
 *     other every tick forever. With it, a row of batteries is a line that carries,
 *     because energy can only ever move towards the emptier end.
 * <li>To anything else, freely, and let {@code receiveEnergy} decide. This half is
 *     not a convenience: a running machine keeps its own buffer near full, so a rule
 *     that compared levels would have a half-full battery refuse the one block in
 *     the world that actually wanted the energy.
 * </ul>
 *
 * <p>The capability is looked up through a cache per side, because a lookup is a map
 * search and this happens twenty times a second for every machine in the world.
 */
public final class Pushing {
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<IEnergyStorage, Direction>[] neighbours =
            new BlockCapabilityCache[Direction.values().length];

    /** Offers up to the store's transfer rate to each side. Returns what left. */
    public int push(ServerLevel level, BlockPos pos, Store store) {
        int rate = store.transferRate();
        int moved = 0;
        if (rate <= 0 || store.isEmpty()) {
            return 0;
        }
        for (Direction side : Direction.values()) {
            IEnergyStorage neighbour = neighbour(level, pos, side);
            if (neighbour == null || !neighbour.canReceive() || !mayOffer(store, neighbour)) {
                continue;
            }
            int offered = Math.min(rate, store.getEnergyStored());
            int taken = neighbour.receiveEnergy(offered, false);
            if (taken > 0) {
                store.extractEnergy(taken, false);
                moved += taken;
            }
            if (store.isEmpty()) {
                break;
            }
        }
        return moved;
    }

    /**
     * Downhill only between two buffers of ours; everything else is somebody else's
     * business. A generator is a {@link Store.Role#SOURCE} and so is never held back:
     * what it makes has nowhere else to go.
     */
    private static boolean mayOffer(Store from, IEnergyStorage to) {
        if (!(to instanceof Store peer)) {
            return true;
        }
        if (from.role() != Store.Role.BUFFER || peer.role() != Store.Role.BUFFER) {
            return true;
        }
        long here = (long) from.getEnergyStored() * peer.getMaxEnergyStored();
        long there = (long) peer.getEnergyStored() * from.getMaxEnergyStored();
        return here > there;
    }

    private IEnergyStorage neighbour(ServerLevel level, BlockPos pos, Direction side) {
        BlockCapabilityCache<IEnergyStorage, Direction> cache = neighbours[side.ordinal()];
        if (cache == null) {
            cache = BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, level,
                    pos.relative(side), side.getOpposite());
            neighbours[side.ordinal()] = cache;
        }
        return cache.getCapability();
    }
}
