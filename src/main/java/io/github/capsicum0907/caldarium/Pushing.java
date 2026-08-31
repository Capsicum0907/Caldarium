package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * The transport layer, entire: on its tick a block offers what it has to the six
 * blocks touching it. There is no route, no plan and no network — a cable here is a
 * block that holds almost nothing and moves a great deal, and this rule carries it.
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
 * <p>{@link Wiring} sits in front of both: it says which side of the boundary
 * between this mod and every other one a neighbour has to be on to be offered to at
 * all.
 */
public final class Pushing {
    private Pushing() {
    }

    /** Offers up to the store's transfer rate to each side. Returns what left. */
    public static int push(Neighbours sides, ServerLevel level, BlockPos pos, Store store) {
        int rate = store.transferRate();
        int moved = 0;
        // ⚠ Nothing that cannot give it up may offer. Without this a sink would hand
        // a neighbour energy and then fail to take it out of itself, which is not a
        // stuck machine but energy made out of nothing.
        if (rate <= 0 || store.isEmpty() || !store.canExtract()) {
            return 0;
        }
        for (Direction side : Direction.values()) {
            IEnergyStorage neighbour = sides.at(level, pos, side);
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
     * The boundary first, then downhill. Downhill holds only between two buffers of
     * ours; everything else is somebody else's business. A generator is a
     * {@link Store.Role#SOURCE} and so is never held back: what it makes has nowhere
     * else to go.
     */
    private static boolean mayOffer(Store from, IEnergyStorage to) {
        if (!from.wiring().mayOffer(Neighbours.ours(to), Neighbours.inLine(to))) {
            return false;
        }
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
