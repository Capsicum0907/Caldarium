package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * The six blocks touching one, as somewhere to put energy or somewhere to take it
 * from. One of these per block entity, shared by {@link Pushing} and {@link Pulling}.
 *
 * <p>The capability is looked up through a cache per side, because a lookup is a map
 * search and this happens twenty times a second for every machine in the world.
 *
 * <p>⚠ One cache, not one per job. An importer both pulls and pushes, and a cache
 * each would be two sets of invalidation listeners watching the same six positions.
 */
public final class Neighbours {
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<IEnergyStorage, Direction>[] sides =
            new BlockCapabilityCache[Direction.values().length];

    /** What is on that side and speaks Forge Energy, or null when nothing does. */
    public IEnergyStorage at(ServerLevel level, BlockPos pos, Direction side) {
        BlockCapabilityCache<IEnergyStorage, Direction> cache = sides[side.ordinal()];
        if (cache == null) {
            cache = BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, level,
                    pos.relative(side), side.getOpposite());
            sides[side.ordinal()] = cache;
        }
        return cache.getCapability();
    }

    /**
     * Whether a neighbour is part of the line. Anything that is not one of ours is
     * outside it by definition — which is the whole of what an importer and an
     * exporter are for.
     */
    public static boolean inLine(IEnergyStorage neighbour) {
        return neighbour instanceof Store peer && peer.wiring().inLine();
    }
}
