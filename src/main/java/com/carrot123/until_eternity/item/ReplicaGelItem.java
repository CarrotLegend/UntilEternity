package com.carrot123.until_eternity.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ReplicaGelItem extends Item {
    public static final int MAX_DURABILITY = 1024;

    public ReplicaGelItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(MAX_DURABILITY)
                .setNoRepair());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canGrindstoneRepair(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
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
                result.getDamageValue(),
                result.getMaxDamage()
        );

        if (nextDamage == ReplicaGelCraftingDurability.BROKEN) {
            return ItemStack.EMPTY;
        }

        result.setDamageValue(nextDamage);
        return result;
    }
}