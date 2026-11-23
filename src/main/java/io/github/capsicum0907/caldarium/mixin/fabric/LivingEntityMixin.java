package io.github.capsicum0907.caldarium.mixin.fabric;

import io.github.capsicum0907.caldarium.Damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
//? if >=1.20.5
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    //? if >=1.20.5 {
    private void caldarium$spoils(ServerLevel level, DamageSource source, CallbackInfo info) {
    //?} else
    //private void caldarium$spoils(DamageSource source, CallbackInfo info) {
        if (Damage.spoiled(source)) {
            info.cancel();
        }
    }
}
