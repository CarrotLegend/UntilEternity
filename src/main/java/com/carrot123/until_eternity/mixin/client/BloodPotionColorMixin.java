package com.carrot123.until_eternity.mixin.client;

import com.carrot123.until_eternity.registry.ModPotions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionUtils.class)
public abstract class BloodPotionColorMixin {
    @Inject(
            method = "getColor(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"),
            cancellable = true)
    private static void untilEternity$useBloodPotionColor(
            ItemStack stack,
            CallbackInfoReturnable<Integer> callback) {
        Potion potion = PotionUtils.getPotion(stack);
        if (potion == ModPotions.DIRTY_BLOOD.get()) {
            callback.setReturnValue(ModPotions.DIRTY_BLOOD_COLOR);
        } else if (potion == ModPotions.EVIL_BLOOD.get()) {
            callback.setReturnValue(ModPotions.EVIL_BLOOD_COLOR);
        } else if (potion == ModPotions.TRUE_BLOOD.get()) {
            callback.setReturnValue(ModPotions.TRUE_BLOOD_COLOR);
        }
    }
}
