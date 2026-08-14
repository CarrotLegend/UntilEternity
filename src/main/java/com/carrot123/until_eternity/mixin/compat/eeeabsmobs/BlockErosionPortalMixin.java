package com.carrot123.until_eternity.mixin.compat.eeeabsmobs;

import com.carrot123.until_eternity.compat.eeeabsmobs.ImmortalPortalHandler;
import com.eeeab.eeeabsmobs.sever.block.BlockErosionPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockErosionPortal.class, remap = false)
abstract class BlockErosionPortalMixin {
    @Inject(
            method = "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = true
    )
    private void untilEternity$handleImmortalPortal(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            CallbackInfo callbackInfo) {
        ImmortalPortalHandler.entityInside(level, pos, entity);
        callbackInfo.cancel();
    }
}
