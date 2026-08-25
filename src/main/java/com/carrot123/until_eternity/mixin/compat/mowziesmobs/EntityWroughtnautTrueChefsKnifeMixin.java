package com.carrot123.until_eternity.mixin.compat.mowziesmobs;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(
        targets = "com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut",
        remap = false)
public abstract class EntityWroughtnautTrueChefsKnifeMixin {
    @ModifyExpressionValue(
            method = {
                    "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;",
                    remap = true),
            remap = false,
            require = 1)
    private Entity untilEternity$hideKnifeAttackerFromWroughtnautGate(
            Entity original,
            DamageSource source,
            float amount) {
        return TrueChefsKnifeAttackContext.matches(
                (LivingEntity) (Object) this, source) ? null : original;
    }

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
    private boolean untilEternity$allowKnifeThroughWroughtnautGate(
            DamageSource source,
            TagKey<DamageType> tag,
            Operation<Boolean> original) {
        boolean tagged = original.call(source, tag);
        return tagged || tag == DamageTypeTags.BYPASSES_INVULNERABILITY
                && TrueChefsKnifeAttackContext.matches(
                (LivingEntity) (Object) this, source);
    }
}
