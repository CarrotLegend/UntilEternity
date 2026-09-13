package com.carrot123.until_eternity.mixin.compat.goetyrevelation;

import com.carrot123.until_eternity.compat.goetyrevelation.GoetyRevelationPuzzleWhitelist;

import net.minecraftforge.event.server.ServerStartedEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = "com.mega.revelationfix.common.event.handler.CommonEventHandler",
        remap = false)
public abstract class CommonEventHandlerPuzzleWhitelistMixin {
    @Inject(
            method = "onLevelLoader(Lnet/minecraftforge/event/server/ServerStartedEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mega/revelationfix/common/odamane/common/TheEndPuzzleItems;bake()V",
                    shift = At.Shift.AFTER,
                    remap = false),
            require = 1,
            remap = false)
    private static void untilEternity$filterPuzzlePool(
            ServerStartedEvent event,
            CallbackInfo ci
    ) {
        GoetyRevelationPuzzleWhitelist.filterBakedPuzzlePool();
    }
}
