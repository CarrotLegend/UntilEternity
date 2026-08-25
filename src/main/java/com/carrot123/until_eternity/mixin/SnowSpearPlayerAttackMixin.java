package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.item.SnowSpear;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class SnowSpearPlayerAttackMixin {
    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 1)
    private boolean untilEternity$replaceSnowSpearDamageSource(
            Entity target,
            DamageSource source,
            float amount,
            Operation<Boolean> original) {
        Player player = (Player) (Object) this;
        DamageSource actualSource = source;
        if (!player.level().isClientSide
                && player.getMainHandItem().is(ModItems.SNOW_SPEAR.get())) {
            actualSource = SnowSpear.frostDamageSource(player.level(), player);
        }
        return original.call(target, actualSource, amount);
    }
}
