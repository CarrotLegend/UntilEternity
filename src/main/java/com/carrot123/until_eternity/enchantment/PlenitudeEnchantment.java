package com.carrot123.until_eternity.enchantment;

import com.carrot123.until_eternity.compat.ironsspellbooks.IronSpellbookTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class PlenitudeEnchantment extends Enchantment {
    private static final EnchantmentCategory STAFFS =
            EnchantmentCategory.create(
                    "until_eternity_plenitude_staff",
                    item -> item.builtInRegistryHolder()
                            .is(IronSpellbookTags.STAFFS));

    public PlenitudeEnchantment() {
        super(Rarity.RARE, STAFFS, new EquipmentSlot[]{
                EquipmentSlot.MAINHAND,
                EquipmentSlot.OFFHAND
        });
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(IronSpellbookTags.STAFFS)
                || stack.is(Items.BOOK)
                || stack.is(Items.ENCHANTED_BOOK);
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }
}
