package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.util.InfinityArrowHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowItem.class)
public abstract class ArrowItemInfinityMixin {
    @Inject(method = "isInfinite", at = @At("HEAD"), cancellable = true, remap = false)
    private void untilEternity$allowAnyArrowItem(
            ItemStack ammo,
            ItemStack bow,
            Player player,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (InfinityArrowHelper.isInfinite(ammo, bow)) {
            callback.setReturnValue(true);
        }
    }
}
