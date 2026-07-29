package com.carrot123.until_eternity.item.curio;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public final class CurioEquipmentHelper {
    private CurioEquipmentHelper() {
    }

    public static int countEquipped(
            LivingEntity entity,
            Item targetItem
    ) {
        return countEquippedExcept(entity, targetItem, null);
    }

    public static int countEquippedExcept(
            LivingEntity entity,
            Item targetItem,
            ItemStack excludedStack
    ) {
        if (entity == null || targetItem == null) {
            return 0;
        }
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.getCurios().values().stream()
                        .mapToInt(stacksHandler -> countIn(
                                stacksHandler.getStacks(),
                                targetItem,
                                excludedStack))
                        .sum())
                .orElse(0);
    }

    private static int countIn(
            IDynamicStackHandler stacks,
            Item targetItem,
            ItemStack excludedStack
    ) {
        int count = 0;
        for (int slot = 0; slot < stacks.getSlots(); slot++) {
            ItemStack equipped = stacks.getStackInSlot(slot);
            if (equipped != excludedStack && equipped.is(targetItem)) {
                count += equipped.getCount();
            }
        }
        return count;
    }
}
