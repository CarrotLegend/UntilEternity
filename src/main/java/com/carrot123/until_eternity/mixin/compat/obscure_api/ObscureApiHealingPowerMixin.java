package com.carrot123.until_eternity.mixin.compat.obscure_api;

import com.obscuria.obscureapi.registry.ObscureAPIAttributes;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ObscureAPIAttributes.class, remap = false)
public abstract class ObscureApiHealingPowerMixin {
    @Inject(
            method = "healEvent(Lnet/minecraftforge/event/entity/living/LivingHealEvent;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false)
    private static void untilEternity$skipObscureHealingPower(
            LivingHealEvent event,
            CallbackInfo callbackInfo
    ) {
        callbackInfo.cancel();
    }
}
