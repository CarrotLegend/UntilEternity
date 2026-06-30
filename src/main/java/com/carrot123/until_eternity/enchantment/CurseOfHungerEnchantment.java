package com.carrot123.until_eternity.enchantment;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 饥饿诅咒 - 可附魔到武器/工具/护甲/饰品
 * 效果: 穿戴/持有时饱食度消耗增加5%
 * 可在战利品和附魔台中获得
 */
public class CurseOfHungerEnchantment extends Enchantment {

    public CurseOfHungerEnchantment() {
        super(Rarity.RARE, ModEnchantments.CURSE_EQUIPPABLE,
                EquipmentSlot.values()); // 所有装备槽都适用
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
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean canEnchant(@Nonnull ItemStack stack) {
        // 可附魔到武器、工具、护甲和单格物品（饰品等）
        return stack.getMaxStackSize() == 1
                && (stack.getItem() instanceof TieredItem
                    || stack.getItem() instanceof ArmorItem
                    || stack.getItem() instanceof Vanishable
                    || canApplyAtEnchantingTable(stack));
    }
}
