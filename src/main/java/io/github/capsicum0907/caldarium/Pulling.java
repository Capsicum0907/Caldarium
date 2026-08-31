package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * The one thing in this mod that takes rather than gives, and the reason an importer
 * exists at all.
 *
 * <p>Everything here pushes, so nothing here has ever needed to be drawn from. Other
 * mods are not all built that way: a generator that waits to be asked would sit full
 * forever beside a line that only ever offers. An importer asks it.
 *
 * <p>It draws across the same boundary an exporter feeds across, and in the other
 * direction — out of anything that is not the line, whoever made it. ⚠ Never out of
 * the line itself: an importer that could take out of a cable would be a second way
 * for energy to move along one, a way that ignores the downhill rule and so has no
 * reason ever to settle.
 *
 * <p>⚠ Drawing out of this mod's own generators as well is not redundancy. A burner
 * pushes, so a cable laid against one needs no importer — but an importer laid against
 * one is the arrangement every mod that has ever had a pipe teaches, and refusing it
 * left a full generator, an empty battery and no way to tell why.
 */
public final class Pulling {
    private Pulling() {
    }

    /**
     * Draws up to the store's transfer rate from the one side it is aimed at.
     *
     * <p>⭐ One side, because asking is the outward half of what an importer does, and
     * a door reaches outside the line on the face it was pointed at when it was placed.
     */
    public static int pull(Neighbours sides, ServerLevel level, BlockPos pos, Store store,
            Direction aimed) {
        // ⚠ Room first, and never more than there is room for. Energy taken out of a
        // neighbour that then will not fit is energy destroyed, and the only sign of it
        // would be a machine emptying with nothing filling.
        int wanted = Math.min(store.transferRate(), store.room());
        if (aimed == null || wanted <= 0) {
            return 0;
        }
        IEnergyStorage neighbour = sides.at(level, pos, aimed);
        if (neighbour == null || !neighbour.canExtract() || Neighbours.inLine(neighbour)) {
            return 0;
        }
        return store.fill(neighbour.extractEnergy(wanted, false));
    }
}
