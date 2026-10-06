package com.carrot123.until_eternity.mixin.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mixin(
        targets = "top.theillusivec4.curios.common.inventory.CurioStacksHandler",
        remap = false
)
public abstract class CurioStacksHandlerShrinkFixMixin {

    @Shadow(remap = false)
    @Final
    private ICuriosItemHandler itemHandler;

    @Inject(
            method = {
                    "loseStacks",
                    "loseStacks(Ltop/theillusivec4/curios/api/type/inventory/IDynamicStackHandler;Ljava/lang/String;I)V"
            },
            at = @At(
                    value = "TAIL",
                    remap = false
            ),
            remap = false,
            require = 1
    )
    private void untilEternity$flushInvalidStacksImmediately(
            IDynamicStackHandler stackHandler,
            String identifier,
            int amount,
            CallbackInfo ci
    ) {
        if (this.itemHandler == null) {
            return;
        }

        LivingEntity wearer = this.itemHandler.getWearer();

        if (wearer == null || wearer.level().isClientSide) {
            return;
        }

        this.itemHandler.handleInvalidStacks();
    }
}