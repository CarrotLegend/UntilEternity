package com.carrot123.until_eternity.mixin.compat.revelationfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(value = LivingEntity.class, priority = 1500)
public abstract class LivingEntityOdamaneTargetingMixin {

    @TargetHandler(
            mixin = "com.mega.revelationfix.mixin.LivingEntityMixin",
            name = "canAttack"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mega/revelationfix/util/entity/ATAHelper2;hasOdamane(Lnet/minecraft/world/entity/Entity;)Z",
                    remap = false
            ),
            require = 1
    )
    private boolean untilEternity$allowHaloPlayerTargeting(
            Entity entity,
            Operation<Boolean> original
    ) {
        return false;
    }
}