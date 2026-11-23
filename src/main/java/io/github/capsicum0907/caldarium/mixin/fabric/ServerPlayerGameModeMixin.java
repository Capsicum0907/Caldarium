package io.github.capsicum0907.caldarium.mixin.fabric;

import io.github.capsicum0907.caldarium.Suns;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Shadow
    @Final
    protected ServerPlayer player;

    @Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;atCenterOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 caldarium$sunSurface(Vec3i pos) {
        //? if >=1.20 {
        return Suns.nearestSurface(player.level(), new BlockPos(pos.getX(), pos.getY(), pos.getZ()), player.getEyePosition());
        //?} else
        /*return Suns.nearestSurface(player.level, new BlockPos(pos.getX(), pos.getY(), pos.getZ()), player.getEyePosition());*/
    }
}
