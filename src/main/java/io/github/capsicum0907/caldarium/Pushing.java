package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class Pushing {
    private Pushing() {
    }

    public static int push(Neighbours sides, ServerLevel level, BlockPos pos, Store store) {
        if (!ready(store)) {
            return 0;
        }
        int moved = 0;
        for (Direction side : Direction.values()) {
            IEnergyStorage neighbour = sides.at(level, pos, side);
            if (neighbour == null || Neighbours.inLine(neighbour) || !Store.accepts(neighbour)
                    || !downhill(store, neighbour)) {
                continue;
            }
            moved += hand(store, neighbour);
            if (store.isEmpty()) {
                break;
            }
        }
        return moved;
    }

    public static int along(Neighbours sides, ServerLevel level, BlockPos pos, Store store) {
        if (!ready(store)) {
            return 0;
        }
        long now = level.getGameTime();
        int moved = 0;
        for (Direction side : Direction.values()) {
            if (store.arrivedRecently(side, now)) {
                continue;
            }
            if (!(sides.at(level, pos, side) instanceof Store peer) || peer.wiring() != Wiring.CABLE
                    || !downhill(store, peer)) {
                continue;
            }
            int taken = hand(store, peer);
            if (taken > 0) {
                peer.arrivedFrom(side.getOpposite(), now);
                moved += taken;
            }
            if (store.isEmpty()) {
                break;
            }
        }
        return moved;
    }

    public static int feed(Neighbours sides, ServerLevel level, BlockPos pos, Store store,
            Direction aimed) {
        if (!ready(store)) {
            return 0;
        }
        long now = level.getGameTime();
        int moved = 0;
        for (Direction side : Direction.values()) {
            if (side == aimed || !(sides.at(level, pos, side) instanceof Store peer)
                    || !store.wiring().feeds(peer.wiring())) {
                continue;
            }
            int taken = hand(store, peer);
            if (taken > 0) {
                peer.arrivedFrom(side.getOpposite(), now);
                moved += taken;
            }
            if (store.isEmpty()) {
                break;
            }
        }
        return moved;
    }

    public static int give(Neighbours sides, ServerLevel level, BlockPos pos, Store store,
            Direction aimed) {
        if (!ready(store) || aimed == null) {
            return 0;
        }
        IEnergyStorage neighbour = sides.at(level, pos, aimed);
        if (neighbour == null || Neighbours.inLine(neighbour) || !Store.accepts(neighbour)) {
            return 0;
        }
        return hand(store, neighbour);
    }

    private static boolean ready(Store store) {
        return store.transferRate() > 0 && !store.isEmpty() && store.canExtract();
    }

    private static int hand(Store from, IEnergyStorage to) {
        int offered = Math.min(from.transferRate(), from.getEnergyStored());
        int taken = to instanceof Store peer ? peer.take(offered) : to.receiveEnergy(offered, false);
        if (taken > 0) {
            from.extractEnergy(taken, false);
        }
        return Math.max(0, taken);
    }

    private static boolean downhill(Store from, IEnergyStorage to) {
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
}
