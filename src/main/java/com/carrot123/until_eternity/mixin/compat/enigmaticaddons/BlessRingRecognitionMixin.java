package com.carrot123.until_eternity.mixin.compat.enigmaticaddons;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = "auviotre.enigmatic.addon.contents.items.BlessRing",
        remap = false
)
public abstract class BlessRingRecognitionMixin {

    @Shadow
    @Final
    public static List<String> blessBetrayalList;

    @Inject(
            method = "<init>",
            at = @At("RETURN"),
            remap = false
    )
    private void untilEternity$removeRecognizedRelicsFromBetrayal(
            CallbackInfo ci
    ) {
        blessBetrayalList.remove(
                "enigmaticlegacy:berserk_charm"
        );

        blessBetrayalList.remove(
                "enigmaticaddons:night_scroll"
        );
    }
}
