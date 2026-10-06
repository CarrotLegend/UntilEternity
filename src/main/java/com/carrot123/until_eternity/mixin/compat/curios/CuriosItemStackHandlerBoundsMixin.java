package com.carrot123.until_eternity.mixin.compat.curios;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

@Mixin(
        value = ItemStackHandler.class,
        remap = false
)
public abstract class CuriosItemStackHandlerBoundsMixin {

    private static final String UNTIL_ETERNITY$DYNAMIC_STACK_HANDLER =
            "top.theillusivec4.curios.common.inventory.DynamicStackHandler";

    @Shadow(remap = false)
    protected NonNullList<ItemStack> stacks;

    @Inject(
            method = {
                    "setStackInSlot",
                    "setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"
            },
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$ignoreRemovedCurioSlotWrite(
            int slot,
            ItemStack stack,
            CallbackInfo ci
    ) {
        if (!untilEternity$isCuriosDynamicHandler()) {
            return;
        }

        if (slot < 0 || slot >= this.stacks.size()) {
            ci.cancel();
        }
    }

    @Inject(
            method = {
                    "getStackInSlot",
                    "getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"
            },
            at = @At(
                    value = "HEAD",
                    remap = false
            ),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void untilEternity$ignoreRemovedCurioSlotRead(
            int slot,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (!untilEternity$isCuriosDynamicHandler()) {
            return;
        }

        if (slot < 0 || slot >= this.stacks.size()) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    private boolean untilEternity$isCuriosDynamicHandler() {
        return UNTIL_ETERNITY$DYNAMIC_STACK_HANDLER.equals(
                this.getClass().getName()
        );
    }
}