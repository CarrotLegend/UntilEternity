package com.carrot123.until_eternity.util;

import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class InfinityArrowHelper {
    private InfinityArrowHelper() {
    }

    public static boolean isInfinite(ItemStack ammo, ItemStack bow) {
        return !ammo.isEmpty()
                && ammo.getItem() instanceof ArrowItem
                && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
    }
}
