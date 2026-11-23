package io.github.capsicum0907.caldarium;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
//? if >=1.20.5
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SolBlockEntity extends BlockEntity {
    private static final double FLIGHT_MARGIN = 4.0;
    private static final int SMOKE = 8;
    private static final int GLOW_REFRESH = 100;

    private static Consumer<SolBlockEntity> leftClient = sol -> {
    };

    public static void onLeavingClient(Consumer<SolBlockEntity> hook) {
        leftClient = hook;
    }

    private int left = -1;

    public SolBlockEntity(BlockPos pos, BlockState state) {
        super(CaldariumRegistry.SOL_ENTITY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            Suns.add(level, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            Suns.remove(level, worldPosition);
            if (level.isClientSide()) {
                leftClient.accept(this);
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null) {
            Suns.remove(level, worldPosition);
            if (level.isClientSide()) {
                leftClient.accept(this);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            SolBlockEntity sol) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        burnWhatIsNear(server, pos, state);
        if (Math.floorMod(server.getGameTime() + pos.asLong(), CaldariumConfig.solReachEvery()) == 0) {
            harmWhatIsInReach(server, pos, state);
        }
        burnWhatFlies(server, pos, state);
        if (Math.floorMod(server.getGameTime() + pos.asLong(), GLOW_REFRESH) == 0) {
            SolBlock.glow(server, pos, state);
        }
        sol.spend(server, pos, state);
    }

    public static void burnWhatFlies(ServerLevel level, BlockPos pos, BlockState state) {
        double radius = SolBlock.radius(state);
        AABB around = new AABB(pos).inflate(Math.ceil(radius) + FLIGHT_MARGIN);
        for (Projectile flying : level.getEntitiesOfClass(Projectile.class, around, Projectile::isAlive)) {
            Vec3 was = new Vec3(flying.xo, flying.yo, flying.zo);
            Vec3 now = flying.position();
            boolean inside = SolBlock.gap(state, pos, now) <= 0.0;
            if (inside || (!was.equals(now) && SolBlock.hit(state, pos, was, now) != null)) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, now.x, now.y, now.z, SMOKE, 0.1, 0.1, 0.1, 0.02);
                level.playSound(null, flying.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                        1.0F, 1.0F);
                flying.discard();
            }
        }
    }

    /**
     * Being near it is being near a fire. Nothing else here hurts anything, so the
     * damage is the ordinary one a fire does rather than a type of this mod's own.
     */
    public static void burnWhatIsNear(ServerLevel level, BlockPos pos, BlockState state) {
        double radius = SolBlock.radius(state);
        double touch = radius + SolBlock.SKIN;
        double reach = Math.max(radius + CaldariumConfig.solBurns(), touch);
        Vec3 centre = Vec3.atCenterOf(pos);
        AABB around = new AABB(pos).inflate(Math.ceil(reach));
        List<LivingEntity> caught = level.getEntitiesOfClass(LivingEntity.class, around,
                living -> nearest(living.getBoundingBox(), centre).distanceToSqr(centre) <= reach * reach);
        for (LivingEntity living : caught) {
            //? if >=1.20.5 {
            living.igniteForSeconds(CaldariumConfig.solBurnSeconds());
            //?} else
            /*living.setSecondsOnFire(CaldariumConfig.solBurnSeconds());*/
            boolean touching = nearest(living.getBoundingBox(), centre).distanceToSqr(centre) <= touch * touch;
            if (touching) {
                Damage.sunburn(level, living, CaldariumConfig.solTouchDamage());
            } else {
                living.hurt(Damage.fire(level), CaldariumConfig.solBurnDamage());
            }
        }
    }

    public static void harmWhatIsInReach(ServerLevel level, BlockPos pos, BlockState state) {
        double reach = SolBlock.radius(state) + CaldariumConfig.solReach();
        Vec3 centre = Vec3.atCenterOf(pos);
        AABB around = new AABB(pos).inflate(Math.ceil(reach));
        List<LivingEntity> within = level.getEntitiesOfClass(LivingEntity.class, around,
                living -> nearest(living.getBoundingBox(), centre).distanceToSqr(centre) <= reach * reach);
        for (LivingEntity living : within) {
            Damage.sunburn(level, living, CaldariumConfig.solReachDamage());
        }
    }


    private static Vec3 nearest(AABB box, Vec3 to) {
        return new Vec3(Mth.clamp(to.x, box.minX, box.maxX), Mth.clamp(to.y, box.minY, box.maxY),
                Mth.clamp(to.z, box.minZ, box.maxZ));
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
        return (int) Math.max(0, Math.min(3, (long) ((whole - left) * 4 / whole)));
    }

    public int left() {
        return left;
    }

    //? if forge {
    /*@Override
    public AABB getRenderBoundingBox() {
        return io.github.capsicum0907.caldarium.client.SolRenderer.bounds(this);
    }
    *///?}

    //? if >=1.20.5 {
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    //?} else {
    /*@Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
    *///?}
        tag.putInt("Left", left);
    }

    //? if >=1.20.5 {
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    //?} else {
    /*@Override
    public void load(CompoundTag tag) {
        super.load(tag);
    *///?}
        left = tag.contains("Left") ? tag.getInt("Left") : -1;
    }
}
