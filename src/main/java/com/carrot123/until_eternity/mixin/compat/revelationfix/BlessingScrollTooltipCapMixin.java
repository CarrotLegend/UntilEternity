package com.carrot123.until_eternity.mixin.compat.revelationfix;

import com.carrot123.until_eternity.compat.goetyrevelation.BlessingScrollDamageCap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.mega.revelationfix.common.item.curios.enigmtic_legacy.BlessingScroll", remap = false)
public abstract class BlessingScrollTooltipCapMixin {
    @ModifyReturnValue(
            method = "lambda$new$5(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/Object;",
            at = @At("RETURN"),
            require = 1,
            remap = false
    )
    private static Object untilEternity$capDisplayedBlessingDamage(Object original,
                                                                   ItemStack stack) {
        if (!(original instanceof Number number)) {
            return original;
        }
        return BlessingScrollDamageCap.capDisplayedPercent(
                stack, number.doubleValue());
    }
}
