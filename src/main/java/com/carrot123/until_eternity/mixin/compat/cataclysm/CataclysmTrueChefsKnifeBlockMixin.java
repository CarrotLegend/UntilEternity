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
        targets = {
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignited_Revenant_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.AcropolisMonsters.Clawdian_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Royal_Draugr_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Kobolediator_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Wadjet_Entity"
        },
        remap = false)
public abstract class CataclysmTrueChefsKnifeBlockMixin {
    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(value = "INVOKE", target = "Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/Ignis_Entity;canBlockDamageSource(Lnet/minecraft/world/damagesource/DamageSource;)Z", remap = false),
            remap = true,
            require = 0)
    private boolean untilEternity$bypassIgnisBlock(
            boolean original, DamageSource source, float amount) {
        return untilEternity$keepBlockForOrdinaryDamage(original, source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(value = "INVOKE", target = "Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/Ignited_Revenant_Entity;canBlockDamageSource(Lnet/minecraft/world/damagesource/DamageSource;)Z", remap = false),
            remap = true,
            require = 0)
    private boolean untilEternity$bypassRevenantBlock(
            boolean original, DamageSource source, float amount) {
        return untilEternity$keepBlockForOrdinaryDamage(original, source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(value = "INVOKE", target = "Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/AcropolisMonsters/Clawdian_Entity;canBlockDamageSource(Lnet/minecraft/world/damagesource/DamageSource;)Z", remap = false),
            remap = true,
            require = 0)
    private boolean untilEternity$bypassClawdianBlock(
            boolean original, DamageSource source, float amount) {
        return untilEternity$keepBlockForOrdinaryDamage(original, source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(value = "INVOKE", target = "Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/Draugar/Royal_Draugr_Entity;canBlockDamageSource(Lnet/minecraft/world/damagesource/DamageSource;)Z", remap = false),
            remap = true,
            require = 0)
    private boolean untilEternity$bypassRoyalDraugrBlock(
            boolean original, DamageSource source, float amount) {
        return untilEternity$keepBlockForOrdinaryDamage(original, source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(value = "INVOKE", target = "Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/Kobolediator_Entity;canBlockDamageSource(Lnet/minecraft/world/damagesource/DamageSource;)Z", remap = false),
            remap = true,
            require = 0)
    private boolean untilEternity$bypassKobolediatorBlock(
            boolean original, DamageSource source, float amount) {
        return untilEternity$keepBlockForOrdinaryDamage(original, source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(value = "INVOKE", target = "Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/Wadjet_Entity;canBlockDamageSource(Lnet/minecraft/world/damagesource/DamageSource;)Z", remap = false),
            remap = true,
            require = 0)
    private boolean untilEternity$bypassWadjetBlock(
            boolean original, DamageSource source, float amount) {
        return untilEternity$keepBlockForOrdinaryDamage(original, source);
    }

    private boolean untilEternity$keepBlockForOrdinaryDamage(
            boolean original, DamageSource source) {
        return original && !TrueChefsKnifeAttackContext.matches(
                (LivingEntity) (Object) this, source);
    }
}
