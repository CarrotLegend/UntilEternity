package com.carrot123.until_eternity.enchantment;

import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class EmpowermentEnchantment extends Enchantment {
    private static final EnchantmentCategory STAFF =
            EnchantmentCategory.create(
                    "until_eternity_empowerment_staff",
                    item -> item instanceof StaffItem);

    public EmpowermentEnchantment() {
        super(Rarity.RARE, STAFF, new EquipmentSlot[]{
                EquipmentSlot.MAINHAND,
                EquipmentSlot.OFFHAND
        });
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 6;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 10;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof StaffItem
                || stack.is(Items.BOOK)
                || stack.is(Items.ENCHANTED_BOOK);
    }
}
