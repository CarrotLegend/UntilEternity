package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class TrueChefsKnifePlayerAttackMixin {
    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeHooks;onPlayerAttackTarget(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)Z",
                    remap = false),
            require = 1)
    private boolean untilEternity$allowKnifeAttackHook(
            Player player,
            Entity target,
            Operation<Boolean> original) {
        boolean allowed = original.call(player, target);
        return allowed || TrueChefsKnifeAttackContext.isEligible(player, target);
    }

    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;isAttackable()Z"),
            require = 1)
    private boolean untilEternity$allowKnifeAttackableGate(
            Entity target,
            Operation<Boolean> original) {
        boolean attackable = original.call(target);
        return attackable || TrueChefsKnifeAttackContext.isEligible((Player) (Object) this, target);
    }

    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;skipAttackInteraction(Lnet/minecraft/world/entity/Entity;)Z"),
            require = 1)
    private boolean untilEternity$bypassKnifeSkipInteraction(
            Entity target,
            Entity attacker,
            Operation<Boolean> original) {
        boolean skipped = original.call(target, attacker);
        return skipped && !TrueChefsKnifeAttackContext.isEligible((Player) (Object) this, target);
    }

    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 1)
    private boolean untilEternity$forceKnifePrimaryHit(
            Entity target,
            DamageSource source,
            float amount,
            Operation<Boolean> original) {
        Player player = (Player) (Object) this;
        if (!TrueChefsKnifeAttackContext.isEligible(player, target)) {
            return original.call(target, source, amount);
        }

        LivingEntity livingTarget = (LivingEntity) target;
        int previousInvulnerableTime = livingTarget.invulnerableTime;
        livingTarget.invulnerableTime = 0;
        boolean hurt = false;
        boolean completed = false;
        try {
            hurt = TrueChefsKnifeAttackContext.withAttack(
                    player,
                    livingTarget,
                    amount,
                    () -> original.call(target, source, amount));
            completed = true;
            return hurt;
        } finally {
            if (!completed || !hurt) {
                livingTarget.invulnerableTime = previousInvulnerableTime;
            }
        }
    }
}
