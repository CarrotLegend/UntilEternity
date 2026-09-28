package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.combat.ArrowForcedDamageHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowDirectHealthMixin {

    @Redirect(
            method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private boolean untilEternity$redirectArrowHurtDev(
            Entity target,
            DamageSource source,
            float amount
    ) {
        return ArrowForcedDamageHelper.hurt(
                target,
                source,
                amount
        );
    }

    @Redirect(
            method = "m_5790_(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    remap = false
            ),
            remap = false,
            require = 0
    )
    private boolean untilEternity$redirectArrowHurtSrg(
            Entity target,
            DamageSource source,
            float amount
    ) {
        return ArrowForcedDamageHelper.hurt(
                target,
                source,
                amount
        );
    }
}