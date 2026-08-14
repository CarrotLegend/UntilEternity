package com.carrot123.until_eternity.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ImFullInventoryHelper {
    private ImFullInventoryHelper() {
    }

    public static boolean hasImFullItem(Player player) {
        if (player == null) {
            return false;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.IMFULL.get())) {
                return true;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(ModItems.IMFULL.get())) {
                return true;
            }
        }
        return false;
    }
}
