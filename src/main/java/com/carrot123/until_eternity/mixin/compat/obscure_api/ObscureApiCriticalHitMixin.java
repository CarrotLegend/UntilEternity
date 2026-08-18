package com.carrot123.until_eternity.mixin.compat.obscure_api;

import com.obscuria.obscureapi.registry.ObscureAPIAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ObscureAPIAttributes.class, remap = false)
public abstract class ObscureApiCriticalHitMixin {
    @Redirect(
            method = "criticalHitAndMagicResistanceEvent(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;random()D",
                    remap = false),
            require = 1,
            remap = false)
    private static double untilEternity$skipObscureCriticalHit() {
        return Double.POSITIVE_INFINITY;
    }
}
