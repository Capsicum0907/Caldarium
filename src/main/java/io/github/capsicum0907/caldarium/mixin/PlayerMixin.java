package io.github.capsicum0907.caldarium.mixin;

import io.github.capsicum0907.caldarium.SolBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "canInteractWithBlock", at = @At("HEAD"), cancellable = true)
    private void caldarium$reachSun(BlockPos pos, double distance, CallbackInfoReturnable<Boolean> result) {
        Player self = (Player) (Object) this;
        BlockState state = self.level().getBlockState(pos);
        if (state.getBlock() instanceof SolBlock) {
            double reach = self.blockInteractionRange() + distance;
            result.setReturnValue(SolBlock.gap(state, pos, self.getEyePosition()) < reach);
        }
    }
}
