package com.carrot123.until_eternity.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * 力量附魔 - 仅武器，最高3级
 * 每级: -0.1 攻击速度, +2% 攻击伤害
 * 可在战利品和附魔台中获得
 */
public class PowerEnchantment extends Enchantment {

    public PowerEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 20;
    }

    /**
     * 与锋利/节肢杀手/亡灵杀手互斥
     */
    @Override
    public boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 仅武器可附魔
        return stack.getItem() instanceof SwordItem || super.canEnchant(stack);
    }
}
