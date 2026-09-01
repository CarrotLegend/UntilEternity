package com.carrot123.until_eternity.mixin.client;

import com.carrot123.until_eternity.client.portal.PortalVisualTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
abstract class NetherPortalVisualMixin {

    @Inject(
            method = {
                    "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V",
                    "m_7892_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V"
            },
            at = @At("HEAD"),
            require = 1,
            remap = false
    )
    private void untilEternity$resetCustomPortalVisual(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            CallbackInfo callbackInfo
    ) {
        PortalVisualTracker.resetIfLocalPlayer(entity);
    }
}