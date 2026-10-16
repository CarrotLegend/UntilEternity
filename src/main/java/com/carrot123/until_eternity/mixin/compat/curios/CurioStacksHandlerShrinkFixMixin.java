package com.carrot123.until_eternity.mixin.compat.curios;

import com.carrot123.until_eternity.compat.curios.CuriosRecoveryStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mixin(targets = "top.theillusivec4.curios.common.inventory.CurioStacksHandler", remap = false)
public abstract class CurioStacksHandlerShrinkFixMixin {
    @Shadow(remap = false) @Final private ICuriosItemHandler itemHandler;
    @Shadow(remap = false) @Final private String identifier;
    @Shadow(remap = false) private IDynamicStackHandler cosmeticStackHandler;

    @Inject(method = {"loseStacks", "loseStacks(Ltop/theillusivec4/curios/api/type/inventory/IDynamicStackHandler;Ljava/lang/String;I)V"},
            at = @At("HEAD"), remap = false, require = 1)
    private void untilEternity$beginSafeShrink(IDynamicStackHandler stackHandler, String slot, int amount, CallbackInfo ci) {
        ServerPlayer player = itemHandler != null && itemHandler.getWearer() instanceof ServerPlayer p ? p : null;
        CuriosRecoveryStorage.beginRemoval(player, slot, Math.max(0, stackHandler.getSlots() - amount));
    }

    @Inject(method = {"loseStacks", "loseStacks(Ltop/theillusivec4/curios/api/type/inventory/IDynamicStackHandler;Ljava/lang/String;I)V"},
            at = @At("RETURN"), remap = false, require = 1)
    private void untilEternity$finishSafeShrink(IDynamicStackHandler stackHandler, String slot, int amount, CallbackInfo ci) {
        CuriosRecoveryStorage.endRemoval();
    }

    @Inject(method = "resize(I)V", at = @At(value = "INVOKE",
            target = "Ltop/theillusivec4/curios/api/type/inventory/IDynamicStackHandler;shrink(I)V",
            ordinal = 1, remap = false), remap = false, require = 1)
    private void untilEternity$preserveCosmeticsBeforeShrink(int newSize, CallbackInfo ci) {
        if (itemHandler == null || !(itemHandler.getWearer() instanceof ServerPlayer player)
                || cosmeticStackHandler == null) return;
        for (int i = Math.max(0, newSize); i < cosmeticStackHandler.getSlots(); i++) {
            ItemStack stack = cosmeticStackHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            CuriosRecoveryStorage.store(player, stack, identifier, i, true);
            cosmeticStackHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
