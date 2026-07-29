package com.carrot123.until_eternity.enchantment;

import net.minecraft.world.item.ItemStack;

public final class EmpowermentLevel {
    public static final double SPELL_POWER_PER_LEVEL = 0.05D;

    private EmpowermentLevel() {
    }

    public static int read(ItemStack stack) {
        if (stack == null || stack.isEmpty()
                || !ModEnchantments.EMPOWERMENT.isPresent()) {
            return 0;
        }
        return ActualEnchantmentLevel.read(
                ModEnchantments.EMPOWERMENT.get(),
                stack);
    }

    public static double bonusForLevel(int level) {
        return level > 0 ? level * SPELL_POWER_PER_LEVEL : 0.0D;
    }
}
