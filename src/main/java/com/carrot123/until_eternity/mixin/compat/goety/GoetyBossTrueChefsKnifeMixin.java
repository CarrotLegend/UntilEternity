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
@Mixin(
        targets = {
                "com.Polarice3.Goety.common.entities.boss.Apostle",
                "com.Polarice3.Goety.common.entities.boss.EnderKeeper",
                "com.Polarice3.Goety.common.entities.boss.Vizier"
        },
        remap = false)
public abstract class GoetyBossTrueChefsKnifeMixin {
    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;moddedInvul:I",
                    opcode = Opcodes.GETFIELD,
                    remap = false),
            remap = true,
            require = 0)
    private int untilEternity$bypassApostleModdedInvulnerability(
            int original, DamageSource source, float amount) {
        return untilEternity$zeroDuringKnifeAttack(original, source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;obsidianInvul:I",
                    opcode = Opcodes.GETFIELD,
                    remap = false),
            remap = true,
            require = 0)
    private int untilEternity$bypassApostleObsidianInvulnerability(
            int original, DamageSource source, float amount) {
        return untilEternity$zeroDuringKnifeAttack(original, source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;isSettingUpSecond()Z",
                    remap = false),
            remap = true,
            require = 0)
    private boolean untilEternity$bypassApostlePhaseSetup(
            boolean original, DamageSource source, float amount) {
        return original && !untilEternity$isKnifeAttack(source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/EnderKeeper;moddedInvul:I",
                    opcode = Opcodes.GETFIELD,
                    remap = false),
            remap = true,
            require = 0)
    private int untilEternity$bypassEnderKeeperModdedInvulnerability(
            int original, DamageSource source, float amount) {
        return untilEternity$zeroDuringKnifeAttack(original, source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/EnderKeeper;isIntro()Z",
                    remap = false),
            remap = true,
            require = 0)
    private boolean untilEternity$bypassEnderKeeperIntro(
            boolean original, DamageSource source, float amount) {
        return original && !untilEternity$isKnifeAttack(source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Vizier;moddedInvul:I",
                    opcode = Opcodes.GETFIELD,
                    remap = false),
            remap = true,
            require = 0)
    private int untilEternity$bypassVizierModdedInvulnerability(
            int original, DamageSource source, float amount) {
        return untilEternity$zeroDuringKnifeAttack(original, source);
    }

    @ModifyExpressionValue(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Vizier;getInvulnerableTicks()I",
                    remap = false),
            remap = true,
            require = 0)
    private int untilEternity$bypassVizierSpawnInvulnerability(
            int original, DamageSource source, float amount) {
        return untilEternity$zeroDuringKnifeAttack(original, source);
    }

    private int untilEternity$zeroDuringKnifeAttack(
            int original, DamageSource source) {
        return untilEternity$isKnifeAttack(source) ? 0 : original;
    }

    private boolean untilEternity$isKnifeAttack(DamageSource source) {
        return TrueChefsKnifeAttackContext.matches(
                (LivingEntity) (Object) this, source);
    }
}
