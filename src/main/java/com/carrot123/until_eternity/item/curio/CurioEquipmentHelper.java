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
        return countEquippedExceptSlot(entity, targetItem, null, -1);
    }

    public static int countEquippedExceptSlot(
            LivingEntity entity,
            Item targetItem,
            String excludedSlotIdentifier,
            int excludedSlotIndex
    ) {
        if (entity == null || targetItem == null) {
            return 0;
        }
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.getCurios().entrySet().stream()
                        .mapToInt(entry -> countIn(
                                entry.getKey(),
                                entry.getValue().getStacks(),
                                targetItem,
                                excludedSlotIdentifier,
                                excludedSlotIndex))
                        .sum())
                .orElse(0);
    }

    public static boolean isEquippedInSlot(
            LivingEntity entity,
            Item targetItem,
            String slotIdentifier
    ) {
        if (entity == null || targetItem == null || slotIdentifier == null) {
            return false;
        }
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.getCurios().get(slotIdentifier))
                .map(handler -> countIn(
                        slotIdentifier,
                        handler.getStacks(),
                        targetItem,
                        null,
                        -1) > 0)
                .orElse(false);
    }

    private static int countIn(
            String slotIdentifier,
            IDynamicStackHandler stacks,
            Item targetItem,
            String excludedSlotIdentifier,
            int excludedSlotIndex
    ) {
        int count = 0;
        for (int slot = 0; slot < stacks.getSlots(); slot++) {
            if (isExcludedSlot(
                    slotIdentifier,
                    slot,
                    excludedSlotIdentifier,
                    excludedSlotIndex)) {
                continue;
            }
            ItemStack equipped = stacks.getStackInSlot(slot);
            if (equipped.is(targetItem)) {
                count += equipped.getCount();
            }
        }
        return count;
    }

    static boolean isExcludedSlot(
            String slotIdentifier,
            int slotIndex,
            String excludedSlotIdentifier,
            int excludedSlotIndex
    ) {
        return excludedSlotIdentifier != null
                && excludedSlotIdentifier.equals(slotIdentifier)
                && excludedSlotIndex == slotIndex;
    }
}
