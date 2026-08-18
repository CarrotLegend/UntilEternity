package com.carrot123.until_eternity.mixin.compat.terra_curio;

import net.minecraft.world.entity.ai.attributes.Attribute;
import org.confluence.terra_curio.event.ForgeEvents;
import org.confluence.terra_curio.misc.ModAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeEvents.class, remap = false)
public abstract class TerraCurioLivingDamageMixin {
    @Redirect(
            method = "livingDamage(Lnet/minecraftforge/event/entity/living/LivingDamageEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/confluence/terra_curio/misc/ModAttributes;hasCustomAttribute(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Z",
                    remap = false),
            require = 1,
            remap = false)
    private static boolean untilEternity$skipTerraDamageCritical(
            Attribute attribute) {
        return true;
    }
}
