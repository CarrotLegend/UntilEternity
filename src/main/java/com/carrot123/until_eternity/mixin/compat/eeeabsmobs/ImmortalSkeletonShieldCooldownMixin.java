package com.carrot123.until_eternity.mixin.compat.eeeabsmobs;

import com.eeeab.eeeabsmobs.sever.entity.immortal.EntityAbsImmortalSkeleton;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityAbsImmortalSkeleton.class, remap = false)
public abstract class ImmortalSkeletonShieldCooldownMixin {

    private static final int SHIELD_COOLDOWN_TICKS = 100;

    @Shadow
    private int blockCoolTick;

    @Inject(
            method = {
                    "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            },
            at = @At("RETURN"),
            remap = false,
            require = 1
    )
    private void untilEternity$applyShieldCooldown(
            DamageSource source,
            float damage,
            CallbackInfoReturnable<Boolean> cir
    ) {
        EntityAbsImmortalSkeleton self =
                (EntityAbsImmortalSkeleton) (Object) this;

        if (self.level().isClientSide) {
            return;
        }

        if (cir.getReturnValueZ()) {
            return;
        }

        if (this.blockCoolTick > 0) {
            return;
        }

        if (!(source.getEntity() instanceof LivingEntity)) {
            return;
        }

        if (source.is(DamageTypeTags.BYPASSES_ARMOR)
                || source.is(DamageTypeTags.BYPASSES_SHIELD)) {
            return;
        }

        if (source.getDirectEntity() instanceof AbstractArrow arrow
                && arrow.getPierceLevel() > 0) {
            return;
        }

        boolean hasShield =
                self.getMainHandItem().getItem() instanceof ShieldItem
                        || self.getOffhandItem().getItem() instanceof ShieldItem;

        if (!hasShield) {
            return;
        }

        if (self.getAnimation() != self.blockAnimation) {
            return;
        }

        this.blockCoolTick = SHIELD_COOLDOWN_TICKS;
    }
}