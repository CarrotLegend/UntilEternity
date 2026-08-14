package com.carrot123.until_eternity.compat.ironsspellbooks;

import com.carrot123.until_eternity.enchantment.ActualEnchantmentLevel;
import com.carrot123.until_eternity.enchantment.ModEnchantments;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.item.ItemStack;

public final class PlenitudeManaCost {
    public static final double REDUCTION_PER_LEVEL = 0.05D;

    private PlenitudeManaCost() {
    }

    public static int effectiveCost(
            int originalCost,
            ItemStack castingStack,
            CastSource castSource
    ) {
        if (originalCost <= 0) {
            return 0;
        }
        if (castSource == null
                || !castSource.consumesMana()
                || castingStack == null
                || castingStack.isEmpty()
                || !castingStack.is(IronSpellbookTags.STAFFS)
                || !ModEnchantments.PLENITUDE.isPresent()) {
            return originalCost;
        }

        int level = ActualEnchantmentLevel.read(
                ModEnchantments.PLENITUDE.get(),
                castingStack);
        return calculate(originalCost, level);
    }

    public static int calculate(int originalCost, int actualLevel) {
        if (originalCost <= 0) {
            return 0;
        }
        if (actualLevel <= 0) {
            return originalCost;
        }

        double multiplier = Math.max(
                0.0D,
                1.0D - REDUCTION_PER_LEVEL * actualLevel);
        int reducedCost = (int) Math.ceil(originalCost * multiplier);
        return Math.max(1, reducedCost);
    }

}
