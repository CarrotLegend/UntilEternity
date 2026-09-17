package com.carrot123.until_eternity.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import top.theillusivec4.curios.api.CuriosApi;

public final class ArmorRendItemEligibility {
    private ArmorRendItemEligibility() {
    }

    public static boolean isEligible(Item item) {
        if (!(item instanceof TieredItem)
                || item instanceof ProjectileWeaponItem
                || item instanceof TridentItem
                || item instanceof ShieldItem
                || item instanceof ArmorItem
                || item instanceof BlockItem) {
            return false;
        }
        ItemStack stack = item.getDefaultInstance();
        if (CuriosApi.getCurio(stack).isPresent()) {
            return false;
        }
        return stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE).stream()
                .anyMatch(modifier -> Double.isFinite(modifier.getAmount())
                        && modifier.getAmount() != 0.0D);
    }
}
