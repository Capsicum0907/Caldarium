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
    //? if >=1.20.5 {
    @Inject(method = "canInteractWithBlock", at = @At("HEAD"), cancellable = true)
    private void caldarium$reachSun(BlockPos pos, double distance, CallbackInfoReturnable<Boolean> result) {
        Player self = (Player) (Object) this;
        BlockState state = self.level().getBlockState(pos);
        if (state.getBlock() instanceof SolBlock) {
            double reach = self.blockInteractionRange() + distance;
            result.setReturnValue(SolBlock.gap(state, pos, self.getEyePosition()) < reach);
        }
    }
    //?} else if forge && >=1.20 {
    /*public boolean canReach(BlockPos pos, double padding) {
        Player self = (Player) (Object) this;
        return caldarium$reaches(self, pos, self.getBlockReach() + padding);
    }

    public boolean canReachRaw(BlockPos pos, double padding) {
        Player self = (Player) (Object) this;
        return caldarium$reaches(self, pos,
                self.getAttributeValue(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get()) + padding);
    }

    private static boolean caldarium$reaches(Player self, BlockPos pos, double reach) {
        BlockState state = self.level().getBlockState(pos);
        if (state.getBlock() instanceof SolBlock) {
            return SolBlock.gap(state, pos, self.getEyePosition()) < reach;
        }
        return self.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) <= reach * reach;
    }
    *///?} else if forge {
    /*public boolean canInteractWith(BlockPos pos, double padding) {
        Player self = (Player) (Object) this;
        double reach = self.getReachDistance() + padding;
        BlockState state = self.level.getBlockState(pos);
        if (state.getBlock() instanceof SolBlock) {
            return SolBlock.gap(state, pos, self.getEyePosition()) < reach;
        }
        return self.getEyePosition().distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < reach * reach;
    }
    *///?}
}
