package com.carrot123.until_eternity.compat.bettercombat;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.item.curio.CurioEquipmentHelper;
import com.carrot123.until_eternity.item.curio.HeadChefGlovesItem;
import net.bettercombat.api.client.AttackRangeExtensions;

public final class HeadChefGlovesBetterCombatCompat {

    private static boolean registered;

    private HeadChefGlovesBetterCombatCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        AttackRangeExtensions.register(context -> {
            var player = context.player();

            if (player == null) {
                return zero();
            }

            if (!player.getMainHandItem().is(
                    HeadChefGlovesItem.KNIVES
            )) {
                return zero();
            }

            if (!CurioEquipmentHelper.isEquippedInSlot(
                    player,
                    ModItems.HEAD_CHEF_GLOVES.get(),
                    "hands"
            )) {
                return zero();
            }

            return new AttackRangeExtensions.Modifier(
                    1.0D,
                    AttackRangeExtensions.Operation.ADD
            );
        });
    }

    private static AttackRangeExtensions.Modifier zero() {
        return new AttackRangeExtensions.Modifier(
                0.0D,
                AttackRangeExtensions.Operation.ADD
        );
    }
}