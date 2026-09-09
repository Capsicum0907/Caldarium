package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;

public final class Lamplight {
    private Lamplight() {
    }

    public static float reaching(ServerLevel level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos.above()) / 15.0F;
    }
}
