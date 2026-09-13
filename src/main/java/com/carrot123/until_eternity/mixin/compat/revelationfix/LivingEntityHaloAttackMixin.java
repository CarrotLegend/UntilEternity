package com.carrot123.until_eternity.mixin.compat.revelationfix;

import com.carrot123.until_eternity.compat.revelationfix.HaloAttackCheckGuard;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 800)
public abstract class LivingEntityHaloAttackMixin {

    @Inject(
            method = {
                    "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z",
                    "m_6779_(Lnet/minecraft/world/entity/LivingEntity;)Z"
            },
            at = @At("HEAD"),
            remap = false,
            require = 1
    )
    private void untilEternity$enterHaloAttackCheck(
            LivingEntity target,
            CallbackInfoReturnable<Boolean> cir
    ) {
        HaloAttackCheckGuard.enter();
    }

    @Inject(
            method = {
                    "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z",
                    "m_6779_(Lnet/minecraft/world/entity/LivingEntity;)Z"
            },
            at = @At("RETURN"),
            remap = false,
            require = 1
    )
    private void untilEternity$exitHaloAttackCheck(
            LivingEntity target,
            CallbackInfoReturnable<Boolean> cir
    ) {
        HaloAttackCheckGuard.exit();
    }
}
