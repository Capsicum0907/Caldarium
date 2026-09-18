package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class Pulling {
    private Pulling() {
    }

    public static int pull(Neighbours sides, ServerLevel level, BlockPos pos, Store store,
            Direction aimed) {
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

    public static int draw(Neighbours sides, ServerLevel level, BlockPos pos, Store store,
            Direction aimed) {
        int drawn = 0;
        for (Direction side : Direction.values()) {
            int wanted = Math.min(store.transferRate() - drawn, store.room());
            if (wanted <= 0) {
                break;
            }
            if (side == aimed || !(sides.at(level, pos, side) instanceof Store peer)
                    || !store.wiring().drawsFrom(peer.wiring())) {
                continue;
            }
            drawn += store.fill(peer.extractEnergy(wanted, false));
        }
        return drawn;
    }
}
