package com.carrot123.until_eternity.mixin.compat.terra_curio;

import com.carrot123.until_eternity.compat.attribute.DodgeChanceBridge;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import org.confluence.terra_curio.misc.ModAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ModAttributes.class, remap = false)
public abstract class TerraCurioDodgeChanceMixin {
    @ModifyExpressionValue(
            method = "applyDodge(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;getValue()D",
                    ordinal = 0,
                    remap = true),
            require = 1,
            remap = false)
    private static double untilEternity$includeResidualObscureDodge(
            double terraChance,
            LivingEntity entity) {
        return DodgeChanceBridge.combineWithResidual(terraChance, entity);
    }
}
