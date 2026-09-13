package com.carrot123.until_eternity.mixin.compat.revelationfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = "com.mega.revelationfix.safe.OdamanePlayerExpandedContext",
        remap = false)
public abstract class OdamaneHealingContextMixin {
    @Inject(
            method = "heal(FLorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false)
    private void untilEternity$useForgeHealingRules(
            float amount,
            CallbackInfo livingHealCallback,
            CallbackInfo ci
    ) {
        // Cancel this helper invocation, not LivingEntity#heal's callback.
        // The vanilla Forge healing path therefore continues normally.
        ci.cancel();
    }
}
