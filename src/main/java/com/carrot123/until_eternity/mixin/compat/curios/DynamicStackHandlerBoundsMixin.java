package com.carrot123.until_eternity.mixin.compat.curios;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mixin(
        targets = "top.theillusivec4.curios.common.inventory.DynamicStackHandler",
        remap = false
)
public abstract class DynamicStackHandlerBoundsMixin {

    @Inject(
            method = {
                    "extractItem",
                    "extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;"
            },
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$ignoreRemovedSlotExtraction(
            int slot,
            int amount,
            boolean simulate,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (untilEternity$isInvalidSlot(slot)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(
            method = {
                    "isItemValid",
                    "isItemValid(ILnet/minecraft/world/item/ItemStack;)Z"
            },
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$ignoreRemovedSlotValidation(
            int slot,
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (untilEternity$isInvalidSlot(slot)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = {
                    "setPreviousStackInSlot",
                    "setPreviousStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"
            },
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$ignoreRemovedPreviousSlotWrite(
            int slot,
            ItemStack stack,
            CallbackInfo ci
    ) {
        if (untilEternity$isInvalidSlot(slot)) {
            ci.cancel();
        }
    }

    @Inject(
            method = {
                    "getPreviousStackInSlot",
                    "getPreviousStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"
            },
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$ignoreRemovedPreviousSlotRead(
            int slot,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (untilEternity$isInvalidSlot(slot)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    private boolean untilEternity$isInvalidSlot(int slot) {
        IDynamicStackHandler handler =
                (IDynamicStackHandler) (Object) this;

        return slot < 0 || slot >= handler.getSlots();
    }
}