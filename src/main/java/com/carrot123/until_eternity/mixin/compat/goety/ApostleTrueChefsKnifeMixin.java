package com.carrot123.until_eternity.mixin.compat.goety;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAttackContext;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.Polarice3.Goety.common.entities.boss.Apostle", remap = false)
public abstract class ApostleTrueChefsKnifeMixin {
    @ModifyExpressionValue(
            method = {
                    "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;moddedInvul:I",
                    opcode = Opcodes.GETFIELD,
                    remap = false),
            remap = false,
            require = 1)
    private int untilEternity$bypassModdedInvulnerability(
            int original, DamageSource source, float amount) {
        return untilEternity$isKnifeAttack(source) ? 0 : original;
    }

    @ModifyExpressionValue(
            method = {
                    "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;obsidianInvul:I",
                    opcode = Opcodes.GETFIELD,
                    remap = false),
            remap = false,
            require = 1)
    private int untilEternity$bypassObsidianInvulnerability(
            int original, DamageSource source, float amount) {
        return untilEternity$isKnifeAttack(source) ? 0 : original;
    }

    private boolean untilEternity$isKnifeAttack(DamageSource source) {
        return TrueChefsKnifeAttackContext.matches(
                (LivingEntity) (Object) this, source);
    }
}
