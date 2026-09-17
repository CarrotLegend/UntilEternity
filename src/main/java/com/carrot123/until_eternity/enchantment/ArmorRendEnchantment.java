package com.carrot123.until_eternity.enchantment;

import java.util.Set;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class ArmorRendEnchantment extends Enchantment {
    private static final EnchantmentCategory MELEE_TOOL = EnchantmentCategory.create(
            "until_eternity_armor_rend", ArmorRendItemEligibility::isEligible);

    public ArmorRendEnchantment() {
        super(Rarity.RARE, MELEE_TOOL, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + 8 * (level - 1);
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return ArmorRendItemEligibility.isEligible(stack.getItem());
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.is(Items.BOOK) || canEnchant(stack);
    }

    @Override
    public boolean allowedInCreativeTab(
            Item item,
            Set<EnchantmentCategory> allowedCategories
    ) {
        return item == Items.ENCHANTED_BOOK && isAllowedOnBooks();
    }
}
