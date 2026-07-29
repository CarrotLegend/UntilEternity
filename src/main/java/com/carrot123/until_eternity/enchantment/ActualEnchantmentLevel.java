package com.carrot123.until_eternity.enchantment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

public final class ActualEnchantmentLevel {
    private ActualEnchantmentLevel() {
    }

    public static int read(Enchantment enchantment, ItemStack stack) {
        if (enchantment == null || stack == null || stack.isEmpty()) {
            return 0;
        }

        int level = Math.max(
                0,
                EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack));
        ResourceLocation enchantmentId =
                ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (enchantmentId == null || !stack.hasTag()) {
            return level;
        }

        CompoundTag root = stack.getTag();
        if (root == null) {
            return level;
        }

        String targetId = enchantmentId.toString();
        level = Math.max(level, readList(
                root.getList("Enchantments", Tag.TAG_COMPOUND),
                targetId));
        level = Math.max(level, readList(
                root.getList("StoredEnchantments", Tag.TAG_COMPOUND),
                targetId));
        return level;
    }

    private static int readList(ListTag enchantments, String targetId) {
        int result = 0;
        for (int index = 0; index < enchantments.size(); index++) {
            CompoundTag entry = enchantments.getCompound(index);
            if (targetId.equals(entry.getString("id"))
                    && entry.contains("lvl", Tag.TAG_ANY_NUMERIC)) {
                result = Math.max(result, Math.max(0, entry.getInt("lvl")));
            }
        }
        return result;
    }
}
