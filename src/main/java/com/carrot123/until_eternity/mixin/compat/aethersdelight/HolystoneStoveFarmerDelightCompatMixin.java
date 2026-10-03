package com.carrot123.until_eternity.mixin.compat.aethersdelight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraftforge.items.IItemHandler;

@Pseudo
@Mixin(
        targets = "net.zjjohn121110.aethersdelight.block.entity.HolystoneStoveBlockEntity",
        remap = false
)
public abstract class HolystoneStoveFarmerDelightCompatMixin {

    @Redirect(
            method = "cookingTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lvectorwing/farmersdelight/common/utility/ItemUtils;isInventoryEmpty(Lnet/minecraftforge/items/IItemHandler;)Z",
                    remap = false
            ),
            remap = false
    )
    private static boolean untilEternity$replaceRemovedInventoryCheck(
            IItemHandler inventory
    ) {
        if (inventory == null) {
            return true;
        }

        for (int slot = 0;
             slot < inventory.getSlots();
             slot++) {
            if (!inventory
                    .getStackInSlot(slot)
                    .isEmpty()) {
                return false;
            }
        }

        return true;
    }
}