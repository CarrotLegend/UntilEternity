package com.carrot123.until_eternity.mixin.compat.aether;

import com.carrot123.until_eternity.combat.GravititePickaxeAttackContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class GravititePickaxePlayerAttackMixin {
    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 1, expect = 1, allow = 1)
    private boolean untilEternity$trackGravititeAttack(
            Entity target, DamageSource source, float amount, Operation<Boolean> original) {
        return GravititePickaxeAttackContext.withAttack((Player) (Object) this, target, source,
                () -> original.call(target, source, amount));
    }
}
