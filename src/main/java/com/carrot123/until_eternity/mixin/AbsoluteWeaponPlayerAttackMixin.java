package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.combat.NetherworldKatanaAttackContext;
import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class AbsoluteWeaponPlayerAttackMixin {
    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 1,
            expect = 1,
            allow = 1)
    private boolean untilEternity$trackAbsoluteWeaponAttack(
            Entity target,
            DamageSource source,
            float amount,
            Operation<Boolean> original
    ) {
        Player player = (Player) (Object) this;
        return TrueChefsKnifeAbsoluteDamageContext.withAttack(
                player,
                target,
                source,
                amount,
                () -> NetherworldKatanaAttackContext.withAttack(
                        player,
                        target,
                        source,
                        amount,
                        () -> original.call(target, source, amount))
        );
    }
}
