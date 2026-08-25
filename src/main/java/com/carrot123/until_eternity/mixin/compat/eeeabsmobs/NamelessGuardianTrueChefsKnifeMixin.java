package com.carrot123.until_eternity.mixin.compat.eeeabsmobs;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(
        targets = "com.eeeab.eeeabsmobs.sever.entity.guling.EntityNamelessGuardian",
        remap = false)
public abstract class NamelessGuardianTrueChefsKnifeMixin {
    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/eeeab/eeeabsmobs/sever/entity/guling/EntityNamelessGuardian;guardianInvulnerableTime:I",
                    opcode = Opcodes.GETFIELD,
                    remap = false),
            remap = true,
            require = 1)
    private int untilEternity$bypassGuardianInvulnerableTime(
            int original,
            DamageSource source,
            float amount) {
        return TrueChefsKnifeAttackContext.matches(
                (LivingEntity) (Object) this, source) ? 0 : original;
    }
}
