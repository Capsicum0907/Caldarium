package io.github.capsicum0907.caldarium.mixin;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.capsicum0907.caldarium.Suns;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "collectColliders", at = @At("RETURN"), cancellable = true)
    private static void caldarium$suns(@Nullable Entity entity, Level level, List<VoxelShape> collisions,
            AABB box, CallbackInfoReturnable<List<VoxelShape>> result) {
        List<VoxelShape> suns = Suns.touching(level, box);
        if (!suns.isEmpty()) {
            List<VoxelShape> all = new ArrayList<>(result.getReturnValue());
            all.addAll(suns);
            result.setReturnValue(all);
        }
    }

    @Inject(method = "pick", at = @At("RETURN"), cancellable = true)
    private void caldarium$pick(double distance, float partial, boolean fluids,
            CallbackInfoReturnable<HitResult> result) {
        Entity self = (Entity) (Object) this;
        Vec3 from = self.getEyePosition(partial);
        Vec3 to = from.add(self.getViewVector(partial).scale(distance));
        BlockHitResult sun = Suns.clip(self.level(), from, to);
        if (sun == null) {
            return;
        }
        HitResult found = result.getReturnValue();
        if (found.getType() == HitResult.Type.MISS
                || from.distanceToSqr(sun.getLocation()) < from.distanceToSqr(found.getLocation())) {
            result.setReturnValue(sun);
        }
    }
}
