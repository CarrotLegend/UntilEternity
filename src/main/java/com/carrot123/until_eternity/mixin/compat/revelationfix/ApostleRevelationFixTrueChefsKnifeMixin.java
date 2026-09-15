package com.carrot123.until_eternity.mixin.compat.revelationfix;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.bawnorton.mixinsquared.TargetHandler;
import com.carrot123.until_eternity.combat.TrueChefsKnifeAbsoluteDamageContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import z1gned.goetyrevelation.config.ModConfig;

/** Targets RevelationFix's merged Apostle#setHealth implementation directly. */
@Pseudo
@Mixin(targets = "com.Polarice3.Goety.common.entities.boss.Apostle",
        priority = 1500, remap = false)
public abstract class ApostleRevelationFixTrueChefsKnifeMixin {
    @TargetHandler(
            mixin = "com.mega.revelationfix.mixin.goety.ApollyonMixin",
            name = "m_21153_(F)V")
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;revelaionfix$getHitCooldown()I",
                    remap = false),
            remap = false, require = 1, expect = 1, allow = 1)
    private int untilEternity$ignoreRevelationFixHitCooldown(
            Apostle instance, Operation<Integer> original) {
        return untilEternity$isKnifeActive() ? 0 : original.call(instance);
    }

    @TargetHandler(
            mixin = "com.mega.revelationfix.mixin.goety.ApollyonMixin",
            name = "m_21153_(F)V")
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE",
                    target = "Lz1gned/goetyrevelation/util/ApollyonAbilityHelper;allTitlesApostle_1_20_1$getHitCooldown()I",
                    remap = false),
            remap = false, require = 1, expect = 1, allow = 1)
    private int untilEternity$ignoreGoetyRevelationHitCooldown(
            Object instance, Operation<Integer> original) {
        return untilEternity$isKnifeActive() ? 0 : original.call(instance);
    }

    @TargetHandler(
            mixin = "com.mega.revelationfix.mixin.goety.ApollyonMixin",
            name = "m_21153_(F)V")
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeConfigSpec$ConfigValue;get()Ljava/lang/Object;",
                    remap = false),
            remap = false, require = 2, expect = 2, allow = 2)
    private Object untilEternity$removeConfiguredHurtLimit(
            ForgeConfigSpec.ConfigValue<?> value, Operation<Object> original) {
        Object result = original.call(value);
        return untilEternity$isKnifeActive() && value == ModConfig.APOLLYON_HURT_LIMIT
                ? Double.POSITIVE_INFINITY
                : result;
    }

    @TargetHandler(
            mixin = "com.mega.revelationfix.mixin.goety.ApollyonMixin",
            name = "m_21153_(F)V")
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"),
            remap = false, require = 2, expect = 2, allow = 2)
    private float untilEternity$removeFinalFloatCaps(
            float damage, float limit, Operation<Float> original) {
        return untilEternity$isKnifeActive()
                ? damage
                : original.call(damage, limit);
    }

    private boolean untilEternity$isKnifeActive() {
        return TrueChefsKnifeAbsoluteDamageContext.isActiveFor(
                (LivingEntity) (Object) this);
    }
}
