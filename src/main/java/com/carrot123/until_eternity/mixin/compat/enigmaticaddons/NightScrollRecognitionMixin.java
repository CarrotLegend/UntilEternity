package com.carrot123.until_eternity.mixin.compat.enigmaticaddons;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(
        targets = "auviotre.enigmatic.addon.contents.items.NightScroll",
        remap = false
)
public abstract class NightScrollRecognitionMixin {

    @Unique
    private boolean untilEternity$recognitionMarker;
}