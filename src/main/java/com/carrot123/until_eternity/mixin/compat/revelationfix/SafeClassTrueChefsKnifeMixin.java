package com.carrot123.until_eternity.mixin.compat.revelationfix;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.mega.revelationfix.common.compat.SafeClass",
        priority = -2400, remap = false)
public abstract class SafeClassTrueChefsKnifeMixin {
    @ModifyReturnValue(
            method = "isDoom(Lcom/Polarice3/Goety/common/entities/boss/Apostle;)Z",
            at = @At("RETURN"), require = 1)
    private static boolean untilEternity$allowCurrentKnifeHit(
            boolean original, Apostle apostle) {
        return TrueChefsKnifeAbsoluteDamageContext.isActiveFor(apostle)
                ? false
                : original;
    }
}
