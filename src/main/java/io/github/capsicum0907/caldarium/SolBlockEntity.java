package io.github.capsicum0907.caldarium;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SolBlockEntity extends BlockEntity {
    private int left = -1;

    public SolBlockEntity(BlockPos pos, BlockState state) {
        super(CaldariumRegistry.SOL_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            SolBlockEntity sol) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        sol.burnWhatIsNear(server, pos);
        sol.spend(server, pos, state);
    }

    /**
     * Being near it is being near a fire. Nothing else here hurts anything, so the
     * damage is the ordinary one a fire does rather than a type of this mod's own.
     */
    private void burnWhatIsNear(ServerLevel level, BlockPos pos) {
        int reach = CaldariumConfig.solBurns();
        if (reach <= 0) {
            return;
        }
        AABB around = new AABB(pos).inflate(reach);
        List<LivingEntity> caught = level.getEntitiesOfClass(LivingEntity.class, around);
        for (LivingEntity living : caught) {
            living.igniteForSeconds(CaldariumConfig.solBurnSeconds());
            living.hurt(level.damageSources().inFire(), CaldariumConfig.solBurnDamage());
        }
    }

    /** Weather spends it faster: a sun in the rain is a sun being put out. */
    private void spend(ServerLevel level, BlockPos pos, BlockState state) {
        if (left < 0) {
            left = CaldariumConfig.solDurability();
        }
        int wet = level.isRainingAt(pos.above()) ? CaldariumConfig.solWeather() : 1;
        left -= wet;
        setChanged();

        if (left <= 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        int spent = worn();
        if (state.getValue(SolBlock.SPENT) != spent) {
            level.setBlock(pos, state.setValue(SolBlock.SPENT, spent), Block.UPDATE_ALL);
        }
    }

    /** Quarters gone, so the block can show it without knowing the numbers. */
    private int worn() {
        int whole = Math.max(1, CaldariumConfig.solDurability());
        return Math.clamp((long) ((whole - left) * 4 / whole), 0, 3);
    }

    public int left() {
        return left;
    }

    /** Whether an artificial sun stands close enough to that place to count as day. */
    public static boolean shining(ServerLevel level, BlockPos where) {
        int reach = CaldariumConfig.solReach();
        for (BlockPos at : BlockPos.betweenClosed(where.offset(-reach, -reach, -reach),
                where.offset(reach, reach, reach))) {
            if (level.getBlockState(at).getBlock() instanceof SolBlock) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Left", left);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        left = tag.contains("Left") ? tag.getInt("Left") : -1;
    }
}
