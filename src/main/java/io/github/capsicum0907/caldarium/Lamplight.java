package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class Lamplight {
    private Lamplight() {
    }

    public static float reaching(ServerLevel level, BlockPos pos) {
        if (Suns.shining(level, pos)) {
            return 1.0F;
        }
        return level.getMaxLocalRawBrightness(pos.above()) / 15.0F;
    }
}
