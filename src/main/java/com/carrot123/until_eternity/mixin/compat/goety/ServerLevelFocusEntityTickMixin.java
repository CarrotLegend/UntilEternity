package com.carrot123.until_eternity.mixin.compat.goety;

import com.carrot123.until_eternity.compat.goety.GoetyFocusCastContext;
import com.carrot123.until_eternity.compat.goety.GoetyFocusDamageMarker;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public abstract class ServerLevelFocusEntityTickMixin {
    @WrapOperation(
            method = "tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;tick()V"),
            require = 1)
    private void untilEternity$restoreFocusCasterWhileTicking(
            Entity entity,
            Operation<Void> original
    ) {
        GoetyFocusDamageMarker.getCasterUuid(entity).ifPresentOrElse(
                casterUuid -> GoetyFocusCastContext.withTrackedCaster(
                        casterUuid, () -> original.call(entity)),
                () -> original.call(entity));
    }
}
