package com.carrot123.until_eternity.compat.ironsspellbooks;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PlenitudeCastingStackResolver {
    private PlenitudeCastingStackResolver() {
    }

    public static ItemStack resolve(Player player, String equipmentSlot) {
        if (player == null || equipmentSlot == null) {
            return ItemStack.EMPTY;
        }
        if (SpellSelectionManager.MAINHAND.equals(equipmentSlot)) {
            return player.getItemBySlot(EquipmentSlot.MAINHAND);
        }
        if (SpellSelectionManager.OFFHAND.equals(equipmentSlot)) {
            return player.getItemBySlot(EquipmentSlot.OFFHAND);
        }
        return ItemStack.EMPTY;
    }

    public static CastSource selectedCastSource(Player player) {
        if (player == null) {
            return CastSource.NONE;
        }
        SpellSelectionManager.SelectionOption selection =
                new SpellSelectionManager(player).getSelection();
        return selection == null
                ? CastSource.NONE
                : selection.getCastSource();
    }
}
