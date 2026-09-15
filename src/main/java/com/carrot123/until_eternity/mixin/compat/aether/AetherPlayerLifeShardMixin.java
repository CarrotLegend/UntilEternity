package com.carrot123.until_eternity.mixin.compat.aether;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Aether 1.20.1-1.5.2: keep the original count, UUID, replacement and synchronization. */
@Pseudo
@Mixin(targets = "com.aetherteam.aether.capability.player.AetherPlayerCapability", remap = false)
public abstract class AetherPlayerLifeShardMixin {
    @ModifyConstant(
            method = "getLifeShardHealthAttributeModifier()Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;",
            constant = @Constant(floatValue = 2.0F),
            remap = false, require = 1, expect = 1, allow = 1)
    private float untilEternity$lifeShardHealthPerUse(float original) {
        return 20.0F;
    }
}
