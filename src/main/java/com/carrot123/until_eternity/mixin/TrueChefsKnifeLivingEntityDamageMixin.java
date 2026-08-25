package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.combat.ForcedHitDamageMath;
import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class TrueChefsKnifeLivingEntityDamageMixin {
    @WrapOperation(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeHooks;onLivingAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    remap = false),
            require = 1)
    private boolean untilEternity$forceKnifeLivingAttack(
            LivingEntity target,
            DamageSource source,
            float amount,
            Operation<Boolean> original) {
        boolean allowed = original.call(target, source, amount);
        return allowed || TrueChefsKnifeAttackContext.matches(target, source);
    }

    @WrapOperation(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z"),
            require = 1)
    private boolean untilEternity$bypassKnifeHurtInvulnerability(
            LivingEntity target,
            DamageSource source,
            Operation<Boolean> original) {
        boolean invulnerable = original.call(target, source);
        return invulnerable && !TrueChefsKnifeAttackContext.matches(target, source);
    }

    @WrapOperation(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"),
            require = 1)
    private boolean untilEternity$bypassKnifeShield(
            LivingEntity target,
            DamageSource source,
            Operation<Boolean> original) {
        boolean blocked = original.call(target, source);
        return blocked && !TrueChefsKnifeAttackContext.matches(target, source);
    }

    @WrapOperation(
            method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z"),
            require = 1)
    private boolean untilEternity$bypassKnifeActualHurtInvulnerability(
            LivingEntity target,
            DamageSource source,
            Operation<Boolean> original) {
        boolean invulnerable = original.call(target, source);
        return invulnerable && !TrueChefsKnifeAttackContext.matches(target, source);
    }

    @WrapOperation(
            method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeHooks;onLivingHurt(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
                    remap = false),
            require = 1)
    private float untilEternity$preserveKnifeDamageAfterHurtHook(
            LivingEntity target,
            DamageSource source,
            float amount,
            Operation<Float> original) {
        float modified = original.call(target, source, amount);
        return TrueChefsKnifeAttackContext.matches(target, source)
                ? ForcedHitDamageMath.preservePositiveHookResult(amount, modified)
                : modified;
    }

    @WrapOperation(
            method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeHooks;onLivingDamage(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
                    remap = false),
            require = 1)
    private float untilEternity$preserveKnifeDamageAfterDamageHook(
            LivingEntity target,
            DamageSource source,
            float amount,
            Operation<Float> original) {
        float modified = original.call(target, source, amount);
        return TrueChefsKnifeAttackContext.matches(target, source)
                ? ForcedHitDamageMath.preservePositiveHookResult(amount, modified)
                : modified;
    }
}
