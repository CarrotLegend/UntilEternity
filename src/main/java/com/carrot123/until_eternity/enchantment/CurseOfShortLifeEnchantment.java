package com.carrot123.until_eternity.enchantment;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 短命诅咒 - 可附魔到武器/工具/护甲/饰品
 * 效果: 如果被附魔物品有耐久，佩戴或持有时每秒减少1点耐久
 * 物品有无法破坏属性或无耐久时不生效
 * 仅在附魔台中可获得
 */
public class CurseOfShortLifeEnchantment extends Enchantment {

    public CurseOfShortLifeEnchantment() {
        super(Rarity.RARE, ModEnchantments.CURSE_EQUIPPABLE,
                EquipmentSlot.values());
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
        return true; // 可在附魔台中获得
    }

    @Override
    public boolean canEnchant(@Nonnull ItemStack stack) {
        // 允许附魔到所有武器/工具/护甲/饰品，无耐久也可以
        // 但功能仅在物品有耐久且非"无法破坏"时才会生效（见 EnchantmentEventHandler）
        return stack.getMaxStackSize() == 1
                && (stack.getItem() instanceof TieredItem
                    || stack.getItem() instanceof ArmorItem
                    || stack.getItem() instanceof Vanishable
                    || canApplyAtEnchantingTable(stack));
    }
}
