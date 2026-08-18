package com.carrot123.until_eternity.mixin.compat.terra_curio;

import net.minecraft.world.entity.ai.attributes.Attribute;
import org.confluence.terra_curio.misc.ModAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModAttributes.class, remap = false)
public abstract class TerraCurioArrowCriticalMixin {
    @Redirect(
            method = "applyToArrow(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/AbstractArrow;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/confluence/terra_curio/misc/ModAttributes;hasCustomAttribute(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Z",
                    ordinal = 1,
                    remap = false),
            require = 1,
            remap = false)
    private static boolean untilEternity$skipTerraArrowCritical(
            Attribute attribute) {
        return true;
    }
}
