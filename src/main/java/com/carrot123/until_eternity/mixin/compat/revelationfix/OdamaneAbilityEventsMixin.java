package com.carrot123.until_eternity.mixin.compat.revelationfix;

import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = "com.mega.revelationfix.common.odamane.common.AbilityEvents",
        remap = false)
public abstract class OdamaneAbilityEventsMixin {
    @Inject(
            method = "onMobChangeTarget(Lnet/minecraftforge/event/entity/living/LivingChangeTargetEvent;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false)
    private static void untilEternity$keepOriginalMobTargeting(
            LivingChangeTargetEvent event,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
