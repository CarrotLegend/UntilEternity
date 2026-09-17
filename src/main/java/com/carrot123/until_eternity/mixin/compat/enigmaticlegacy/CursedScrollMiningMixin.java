package com.carrot123.until_eternity.mixin.compat.enigmaticlegacy;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler", remap = false)
public abstract class CursedScrollMiningMixin {
    @ModifyExpressionValue(
            method = "miningStuff(Lnet/minecraftforge/event/entity/player/PlayerEvent$BreakSpeed;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/aizistral/enigmaticlegacy/handlers/SuperpositionHandler;getCurseAmount(Lnet/minecraft/world/entity/player/Player;)I",
                    remap = false),
            require = 1,
            remap = false)
    private int untilEternity$removeCursedScrollMiningSpeed(int curseCount) {
        return 0;
    }
}
