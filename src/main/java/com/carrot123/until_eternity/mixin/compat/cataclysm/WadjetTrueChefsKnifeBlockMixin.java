package com.carrot123.until_eternity.mixin.compat.cataclysm;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(
        targets = "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Wadjet_Entity",
        remap = false)
public abstract class WadjetTrueChefsKnifeBlockMixin {
    @ModifyExpressionValue(
            method = {
                    "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/Wadjet_Entity;canBlockDamageSource(Lnet/minecraft/world/damagesource/DamageSource;)Z",
                    remap = false),
            remap = false,
            require = 1)
    private boolean untilEternity$bypassKnifeBlock(
            boolean original, DamageSource source, float amount) {
        return original && !TrueChefsKnifeAttackContext.matches(
                (LivingEntity) (Object) this, source);
    }
}
