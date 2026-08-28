package com.carrot123.until_eternity.enchantment;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * A genuine curse that intentionally has no gameplay effect.
 */
public final class UselessCurseEnchantment extends Enchantment {
    public UselessCurseEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE,
                EquipmentSlot.values());
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 25;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public Component getFullname(int level) {
        Component vanillaName = super.getFullname(level);
        if (level != 1) {
            return vanillaName;
        }
        return vanillaName.copy()
                .append(CommonComponents.SPACE)
                .append(Component.translatable("enchantment.level.1"));
    }
}
