package com.carrot123.until_eternity.mixin.compat.cataclysm;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(
        targets = {
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Amethyst_Crab_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Golem_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.LLibrary_Boss_Monster",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity",
                "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Aptrgangr_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.IABoss_monster",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Kobolediator_Entity",
                "com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Wadjet_Entity"
        },
        remap = false)
public abstract class CataclysmTrueChefsKnifeDamageGateMixin {
    @WrapOperation(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                    remap = true),
            remap = true,
            require = 1)
    private boolean untilEternity$useExistingInvulnerabilityBypassBranches(
            DamageSource source,
            TagKey<DamageType> tag,
            Operation<Boolean> original) {
        boolean tagged = original.call(source, tag);
        return tagged || tag == DamageTypeTags.BYPASSES_INVULNERABILITY
                && TrueChefsKnifeAttackContext.matches(
                (LivingEntity) (Object) this, source);
    }

    @WrapOperation(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z",
                    remap = true),
            remap = true,
            require = 0)
    private boolean untilEternity$bypassPreSuperInvulnerabilityCheck(
            LivingEntity target,
            DamageSource source,
            Operation<Boolean> original) {
        boolean invulnerable = original.call(target, source);
        return invulnerable && !TrueChefsKnifeAttackContext.matches(target, source);
    }
}
