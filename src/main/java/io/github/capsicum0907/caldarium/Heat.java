package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class Heat {
    private Heat() {
    }

    public static float reaching(ServerLevel level, BlockPos pos) {
        float total = 0.0F;
        for (Direction.Axis axis : Direction.Axis.values()) {
            float positive = at(level, pos.relative(
                    Direction.get(Direction.AxisDirection.POSITIVE, axis)));
            float negative = at(level, pos.relative(
                    Direction.get(Direction.AxisDirection.NEGATIVE, axis)));
            total += Math.abs(positive - negative);
        }
        float span = CaldariumConfig.heatSpan();
        return span <= 0.0F ? 0.0F : total / span;
    }

    private static float at(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Float given = CaldariumConfig.temperature(state);
        return given != null ? given : level.getBiome(pos).value().getBaseTemperature();
    }
}
