package com.carrot123.until_eternity.mixin.compat.goety;

import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
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
            method = {"hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"},
            at = @At(value = "FIELD",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;moddedInvul:I",
                    opcode = Opcodes.GETFIELD, remap = false),
            remap = false, require = 1, expect = 1, allow = 1)
    private int untilEternity$ignoreHurtCooldown(int original,
                                                DamageSource source, float amount) {
        return untilEternity$isKnife(source) ? 0 : original;
    }

    @ModifyExpressionValue(
            method = {"hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"},
            at = @At(value = "FIELD",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;obsidianInvul:I",
                    opcode = Opcodes.GETFIELD, remap = false),
            remap = false, require = 1, expect = 1, allow = 1)
    private int untilEternity$ignoreObsidianCooldown(int original,
                                                    DamageSource source, float amount) {
        return untilEternity$isKnife(source) ? 0 : original;
    }

    @ModifyExpressionValue(
            method = {"hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"},
            at = @At(value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;isInNether()Z",
                    ordinal = 0, remap = false),
            remap = false, require = 1, expect = 1, allow = 1)
    private boolean untilEternity$skipNetherReduction(boolean original,
                                                      DamageSource source, float amount) {
        return untilEternity$isKnife(source) ? false : original;
    }

    @ModifyExpressionValue(
            method = {"actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V",
                    "m_6475_(Lnet/minecraft/world/damagesource/DamageSource;F)V"},
            at = @At(value = "FIELD",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;moddedInvul:I",
                    opcode = Opcodes.GETFIELD, remap = false),
            remap = false, require = 1, expect = 1, allow = 1)
    private int untilEternity$ignoreActuallyHurtCooldown(int original,
                                                        DamageSource source, float amount) {
        return untilEternity$isKnife(source) ? 0 : original;
    }

    private boolean untilEternity$isKnife(DamageSource source) {
        return TrueChefsKnifeAbsoluteDamageContext.matches(
                (LivingEntity) (Object) this, source);
    }
}
