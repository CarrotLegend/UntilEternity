package com.carrot123.until_eternity.mixin.client;

import com.carrot123.until_eternity.util.RainbowTextHelper;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public abstract class FontRainbowMixin {

    @ModifyVariable(
            method = {
                    "drawInBatch(" +
                            "Lnet/minecraft/util/FormattedCharSequence;" +
                            "FFIZ" +
                            "Lorg/joml/Matrix4f;" +
                            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
                            "Lnet/minecraft/client/gui/Font$DisplayMode;" +
                            "II" +
                            ")I",

                    "m_272191_(" +
                            "Lnet/minecraft/util/FormattedCharSequence;" +
                            "FFIZ" +
                            "Lorg/joml/Matrix4f;" +
                            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
                            "Lnet/minecraft/client/gui/Font$DisplayMode;" +
                            "II" +
                            ")I"
            },
            at = @At("HEAD"),
            argsOnly = true,
            remap = false,
            require = 1
    )
    private FormattedCharSequence
    untilEternity$applyRainbowToDraw(
            FormattedCharSequence text
    ) {
        return RainbowTextHelper.transform(text);
    }

    @ModifyVariable(
            method = {
                    "drawInBatch8xOutline(" +
                            "Lnet/minecraft/util/FormattedCharSequence;" +
                            "FFII" +
                            "Lorg/joml/Matrix4f;" +
                            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
                            "I" +
                            ")V",

                    "m_168645_(" +
                            "Lnet/minecraft/util/FormattedCharSequence;" +
                            "FFII" +
                            "Lorg/joml/Matrix4f;" +
                            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
                            "I" +
                            ")V"
            },
            at = @At("HEAD"),
            argsOnly = true,
            remap = false,
            require = 1
    )
    private FormattedCharSequence
    untilEternity$applyRainbowToOutline(
            FormattedCharSequence text
    ) {
        return RainbowTextHelper.transform(text);
    }

    @ModifyVariable(
            method = {
                    "width(" +
                            "Lnet/minecraft/util/FormattedCharSequence;" +
                            ")I",

                    "m_92724_(" +
                            "Lnet/minecraft/util/FormattedCharSequence;" +
                            ")I"
            },
            at = @At("HEAD"),
            argsOnly = true,
            remap = false,
            require = 1
    )
    private FormattedCharSequence
    untilEternity$applyRainbowToWidth(
            FormattedCharSequence text
    ) {
        return RainbowTextHelper.transform(text);
    }
}