package com.carrot123.until_eternity.mixin.compat.revelationfix;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.mega.revelationfix.common.apollyon.common.AttackDamageChangeHandler",
        priority = -2400, remap = false)
public abstract class AttackDamageChangeHandlerTrueChefsKnifeMixin {
    @Shadow @Final public Apostle apostle;

    @ModifyReturnValue(
            method = "redirectActuallyHurtAmount(F)F",
            at = @At("RETURN"), require = 1)
    private float untilEternity$keepOriginalAmount(float reduced, float amount) {
        return TrueChefsKnifeAbsoluteDamageContext.isActiveFor(apostle)
                ? amount
                : reduced;
    }
}
