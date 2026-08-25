package com.carrot123.until_eternity.mixin.compat.eeeabsmobs;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.eeeab.eeeabsmobs.sever.util.EMTagKey;
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
                "com.eeeab.eeeabsmobs.sever.entity.corpse.EntityCorpseWarlock",
                "com.eeeab.eeeabsmobs.sever.entity.guling.EntityGulingSentinel",
                "com.eeeab.eeeabsmobs.sever.entity.guling.EntityNamelessGuardian",
                "com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortal",
                "com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortalExecutioner",
                "com.eeeab.eeeabsmobs.sever.entity.immortal.EntityImmortalShaman"
        },
        remap = false)
public abstract class EeeabTrueChefsKnifeDamageGateMixin {
    @WrapOperation(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                    remap = true),
            remap = true,
            require = 0)
    private boolean untilEternity$useExistingUnresistantDamageBranches(
            DamageSource source,
            TagKey<DamageType> tag,
            Operation<Boolean> original) {
        boolean tagged = original.call(source, tag);
        return tagged || TrueChefsKnifeAttackContext.matches(
                (LivingEntity) (Object) this, source)
                && (tag == DamageTypeTags.BYPASSES_INVULNERABILITY
                || tag == EMTagKey.GENERAL_UNRESISTANT_TO);
    }
}
