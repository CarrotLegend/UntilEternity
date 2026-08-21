package com.carrot123.until_eternity.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ReplicaGelItem extends Item {
    public static final int MAX_DURABILITY = 1024;

    public ReplicaGelItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(MAX_DURABILITY));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack result = stack.copy();
        result.setCount(1);

        int nextDamage = ReplicaGelCraftingDurability.nextDamageOrBroken(
                result.getDamageValue(), result.getMaxDamage());
        if (nextDamage == ReplicaGelCraftingDurability.BROKEN) {
            return ItemStack.EMPTY;
        }

        result.setDamageValue(nextDamage);
        return result;
    }
}
