package io.github.capsicum0907.caldarium.mixin.legacy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LightningBolt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightningBolt.class)
public interface LightningBoltAccessor {
    @Accessor("cause")
    ServerPlayer caldarium$cause();
}
