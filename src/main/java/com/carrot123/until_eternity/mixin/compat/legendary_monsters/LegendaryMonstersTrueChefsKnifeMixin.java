package com.carrot123.until_eternity.mixin.compat.legendary_monsters;

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
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Chorusling.TheWarpedOne.TheWarpedOneOld",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.CloudGolem.Cloud_GolemEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.PossessedPaladin.NewPossessedPaladin",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.PossessedPaladin.PossessedPaladinEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.TheObliterator.TheObliteratorEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.AncientStronghold.Ancient_GuardianEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Chorusling.EndersentEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.CollapsedKingdom.OldKnights.HauntedKnightEntityOld",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.CollapsedKingdom.PosessedPaladinEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Frostbitten_GolemEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Lava_eaterEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Overgrown_colossusEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.SkeletosaurusEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.SpaceStation.Flameborn.AnnihilationPursuer.AnnihilationPursuerEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.Mobs.Withered_AbominationEntity",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.OriginClasses.IAnimatedBoss",
                "net.miauczel.legendary_monsters.entity.AnimatedMonster.OriginClasses.IAnimatedMiniBoss"
        },
        remap = false)
public abstract class LegendaryMonstersTrueChefsKnifeMixin {
    @WrapOperation(
            method = {
                    "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                    remap = true),
            remap = false,
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
}
