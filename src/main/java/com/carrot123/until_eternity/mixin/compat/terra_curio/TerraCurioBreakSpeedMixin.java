package com.carrot123.until_eternity.mixin.compat.terra_curio;

import net.minecraft.world.entity.ai.attributes.Attribute;
import org.confluence.terra_curio.event.PlayerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PlayerEvents.class, remap = false)
public abstract class TerraCurioBreakSpeedMixin {
    @Redirect(
            method = "breakSpeed(Lnet/minecraftforge/event/entity/player/PlayerEvent$BreakSpeed;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/confluence/terra_curio/misc/ModAttributes;hasCustomAttribute(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Z",
                    ordinal = 0,
                    remap = false),
            require = 1,
            remap = false)
    private static boolean untilEternity$skipTerraMiningSpeed(
            Attribute attribute) {
        return true;
    }
}
