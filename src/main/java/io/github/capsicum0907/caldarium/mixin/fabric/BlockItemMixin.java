package io.github.capsicum0907.caldarium.mixin.fabric;

import io.github.capsicum0907.caldarium.Suns;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void caldarium$intoSun(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> result) {
        if (Suns.inside(context.getLevel(), context.getClickedPos())) {
            result.setReturnValue(InteractionResult.FAIL);
        }
    }
}
