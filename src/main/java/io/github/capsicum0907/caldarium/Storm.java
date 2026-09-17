package io.github.capsicum0907.caldarium;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;

/**
 * Catching a strike.
 *
 * <p>⚠ <b>Not {@code LightningRodBlock#onLightningStrike}.</b> That hook is handed the
 * block and the place and not the bolt, so it cannot say who called the lightning down
 * - and telling a storm from a trident is the whole of why this reads the bolt itself.
 *
 * <p>⚠ <b>And what it can tell is one-sided.</b> A cause is set in exactly one place in
 * the game, by the enchantment that summons lightning; weather sets none, a command
 * sets none, a trapped skeleton horse sets none, and another mod has no reason to set
 * one either. So a cause present means summoned, and a cause absent means only that
 * nobody said. Asked for and answered: no cooldown guards the rest, because somebody
 * who has gone as far as a mod that calls lightning down has earned it.
 */
public final class Storm {
    private Storm() {
    }

    private static final Set<LightningBolt> COUNTED = Collections.newSetFromMap(new WeakHashMap<>());

    public static void struck(LightningBolt bolt) {
        Level level = bolt.level();
        if (level.isClientSide() || !COUNTED.add(bolt)) {
            return;
        }
        boolean summoned = bolt.getCause() != null;
        int reach = CaldariumConfig.stormReach();
        BlockPos at = bolt.blockPosition();
        for (BlockPos near : BlockPos.betweenClosed(at.offset(-reach, -reach, -reach),
                at.offset(reach, reach, reach))) {
            if (level.getBlockEntity(near) instanceof GeneratorBlockEntity generator) {
                generator.strike(summoned);
            }
        }
    }
}
