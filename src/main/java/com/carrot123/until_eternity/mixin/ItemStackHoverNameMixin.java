package com.carrot123.until_eternity.mixin;

import com.carrot123.until_eternity.compat.ironsspellbooks.StaffAffixHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackHoverNameMixin {
    @Inject(
            method = "getHoverName()Lnet/minecraft/network/chat/Component;",
            at = @At("RETURN"),
            cancellable = true,
            require = 1
    )
    private void untilEternity$composeStaffDisplayName(
            CallbackInfoReturnable<Component> callback
    ) {
        ItemStack stack = (ItemStack) (Object) this;
        callback.setReturnValue(
                StaffAffixHelper.composeHoverName(
                        stack, callback.getReturnValue().copy()));
    }
}
