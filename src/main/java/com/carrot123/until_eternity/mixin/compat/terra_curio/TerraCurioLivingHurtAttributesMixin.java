package com.carrot123.until_eternity.mixin.compat.terra_curio;

import net.minecraft.world.damagesource.DamageSource;
import org.confluence.terra_curio.event.ForgeEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeEvents.class, remap = false)
public abstract class TerraCurioLivingHurtAttributesMixin {
    @Redirect(
            method = "livingHurt(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/confluence/terra_curio/misc/ModAttributes;applyMagicDamage(Lnet/minecraft/world/damagesource/DamageSource;F)F",
                    remap = false),
            require = 1,
            remap = false)
    private static float untilEternity$skipTerraMagicDamage(
            DamageSource source,
            float amount) {
        return amount;
    }

    @Redirect(
            method = "livingHurt(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/confluence/terra_curio/misc/ModAttributes;applyRangedDamage(Lnet/minecraft/world/damagesource/DamageSource;F)F",
                    remap = false),
            require = 1,
            remap = false)
    private static float untilEternity$skipTerraRangedDamage(
            DamageSource source,
            float amount) {
        return amount;
    }
}
