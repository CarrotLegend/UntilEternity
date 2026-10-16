package com.carrot123.until_eternity.mixin.compat.curios;

import com.carrot123.until_eternity.compat.curios.CuriosRecoveryStorage;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "top.theillusivec4.curios.common.capability.CurioInventoryCapability$CurioInventoryWrapper", remap = false)
public abstract class CurioInventoryInvalidStackProtectionMixin {
    @Shadow(remap = false) private NonNullList<ItemStack> invalidStacks;
    @Shadow(remap = false) private LivingEntity wearer;

    @Inject(method = {"loseInvalidStack", "loseInvalidStack(Lnet/minecraft/world/item/ItemStack;)V"},
            at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void untilEternity$captureShrinkItem(ItemStack stack, CallbackInfo ci) {
        if (CuriosRecoveryStorage.interceptInvalidStack(stack)) ci.cancel();
    }

    @Inject(method = "handleInvalidStacks()V", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void untilEternity$protectOtherInvalidItems(CallbackInfo ci) {
        if (!(wearer instanceof ServerPlayer player)) return;
        for (ItemStack stack : invalidStacks) {
            if (!stack.isEmpty()) CuriosRecoveryStorage.store(player, stack, "", -1, false);
        }
        invalidStacks.clear();
        ci.cancel();
    }
}
