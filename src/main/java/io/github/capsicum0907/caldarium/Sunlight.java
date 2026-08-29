package io.github.capsicum0907.caldarium;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * How much of the sky reaches a block.
 *
 * <p>Three questions, in the order that lets the cheap ones answer first: is it day,
 * is the weather clear, and is there anything overhead. Nothing else — no season, no
 * angle of the sun, no biome.
 *
 * <p>⚠ Anything overhead that light passes through takes a share rather than all of
 * it, so a panel under glass still works and works less well. Anything solid takes
 * all of it. The share is a setting because there is no defensible number for it.
 */
public final class Sunlight {
    private Sunlight() {
    }

    /** What is left of the generation rate here, as a fraction of one. */
    public static float reaching(ServerLevel level, BlockPos pos) {
        if (!level.isDay() || level.isRaining() || level.isThundering()) {
            return 0.0F;
        }
        float left = 1.0F;
        float through = CaldariumConfig.SUN_THROUGH.get() / 100.0F;

        // ⚠ Bounded by the heightmap rather than by the top of the world: the loop is
        // as long as the stack of blocks actually above this one, which is normally
        // none at all. Walking to the build limit twenty times a second per panel is
        // the version of this that ends up in somebody's performance report.
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        for (BlockPos above = pos.above(); above.getY() <= top; above = above.above()) {
            var state = level.getBlockState(above);
            if (state.isAir()) {
                continue;
            }
            if (state.isSolidRender(level, above)) {
                return 0.0F;
            }
            left *= through;
        }
        return left;
    }
}
