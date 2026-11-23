package io.github.capsicum0907.caldarium.mixin;

import io.github.capsicum0907.caldarium.Storm;

import net.minecraft.world.entity.LightningBolt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void caldarium$struck(CallbackInfo info) {
        Storm.struck((LightningBolt) (Object) this);
    }
}
