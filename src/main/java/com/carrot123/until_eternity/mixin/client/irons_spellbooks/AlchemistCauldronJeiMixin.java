package com.carrot123.until_eternity.mixin.client.irons_spellbooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(
        targets = "io.redspace.ironsspellbooks.jei.AlchemistCauldronRecipeMaker",
        remap = false
)
public abstract class AlchemistCauldronJeiMixin {

    @Inject(
            method = "getScrollRecipes",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private static void untilEternity$removeScrollRecyclingRecipes(
            CallbackInfoReturnable<Stream<?>> cir
    ) {
        cir.setReturnValue(Stream.empty());
    }
}