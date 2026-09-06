package com.carrot123.until_eternity.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

public final class BloodBottleHelper {
    private BloodBottleHelper() {
    }

    public static boolean consumeOffhandBottleAndGive(
            Player player,
            Potion bloodPotion) {
        if (player.level().isClientSide) {
            return false;
        }

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.is(Items.GLASS_BOTTLE)) {
            return false;
        }

        ItemStack blood = PotionUtils.setPotion(
                new ItemStack(Items.POTION),
                bloodPotion);
        if (offhand.getCount() == 1) {
            player.setItemInHand(InteractionHand.OFF_HAND, blood);
        } else {
            offhand.shrink(1);
            if (!player.getInventory().add(blood) && !blood.isEmpty()) {
                player.drop(blood, false);
            }
        }
        return true;
    }
}
