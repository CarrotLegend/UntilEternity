package com.carrot123.until_eternity.mixin.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AlchemistCauldronTile.class, remap = false)
public abstract class AlchemistCauldronScrollMixin {

    @Inject(
            method = "isValidInput",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void untilEternity$disableScrollInput(
            ItemStack itemStack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (itemStack.is(ItemRegistry.SCROLL.get())) {
            cir.setReturnValue(false);
        }
    }
    @Inject(
            method = "tryMeltInput",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void untilEternity$disableScrollRecycling(
            ItemStack itemStack,
            CallbackInfo ci
    ) {
        if (itemStack.is(ItemRegistry.SCROLL.get())) {
            ci.cancel();
        }
    }
}