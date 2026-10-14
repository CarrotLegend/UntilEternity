package com.carrot123.until_eternity.mixin.compat.eeeabsmobs;

import com.eeeab.eeeabsmobs.sever.entity.EEEABMobEntity;
import com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortal;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EEEABMobEntity.class, remap = false)
public abstract class ImmortalDamageCapMixin {

    @WrapOperation(
            method = "getNewHealthByCap(FLcom/eeeab/eeeabsmobs/sever/config/EMConfigHandler$DamageCapConfig;)F",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F", remap = false),
            require = 2,
            expect = 2,
            allow = 2,
            remap = false
    )
    private float untilEternity$removeImmortalDamageCap(
            float incomingDamage,
            float maximumDamage,
            Operation<Float> original
    ) {
        if ((Object) this instanceof EntityImmortal) {
            return incomingDamage;
        }
        return original.call(incomingDamage, maximumDamage);
    }
}