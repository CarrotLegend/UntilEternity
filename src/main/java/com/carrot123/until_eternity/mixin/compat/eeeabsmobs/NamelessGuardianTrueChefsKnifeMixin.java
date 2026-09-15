package com.carrot123.until_eternity.mixin.compat.eeeabsmobs;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.eeeab.eeeabsmobs.sever.entity.guling.EntityNamelessGuardian",
        remap = false)
public abstract class NamelessGuardianTrueChefsKnifeMixin {
    @ModifyExpressionValue(
            method = {"hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"},
            at = @At(value = "FIELD",
                    target = "Lcom/eeeab/eeeabsmobs/sever/entity/guling/EntityNamelessGuardian;guardianInvulnerableTime:I",
                    opcode = Opcodes.GETFIELD, ordinal = 0, remap = false),
            remap = false, require = 1, expect = 1, allow = 1)
    private int untilEternity$ignoreInitialCooldown(int original,
                                                   DamageSource source, float damage) {
        return untilEternity$isKnife(source) ? 0 : original;
    }

    @WrapOperation(
            method = {"hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"},
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"),
            remap = false, require = 1, expect = 1, allow = 1)
    private float untilEternity$ignoreDamageCap(float damage, float cap,
                                               Operation<Float> original,
                                               DamageSource source) {
        return untilEternity$isKnife(source) ? damage : original.call(damage, cap);
    }

    private boolean untilEternity$isKnife(DamageSource source) {
        return TrueChefsKnifeAbsoluteDamageContext.matches(
                (LivingEntity) (Object) this, source);
    }
}
