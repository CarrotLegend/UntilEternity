package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.effect.KingdomComeOwnerTracker;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.item.curio.CurioEquipmentHelper;
import com.carrot123.until_eternity.item.curio.charm.RedemptionCharmItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

final class RedemptionCharmDamageLogic {
    private RedemptionCharmDamageLogic() {
    }

    static boolean hasActiveCharm(ServerPlayer player, Item item) {
        return isEquipped(player, item);
    }

    static double amplify(ServerPlayer attacker, LivingEntity target, double damage) {
        double luck = attacker.getAttributeValue(Attributes.LUCK);
        if (!Double.isFinite(luck)) {
            luck = 0.0D;
        }
        if (isEquipped(attacker, ModItems.REDEMPTION_STAR.get())) {
            double bonus = Math.min(1.0D, Math.floor(Math.max(0.0D, luck) / 10.0D) * 0.05D);
            damage *= 1.0D + bonus;
        }
        if (isEquipped(attacker, ModItems.UNSTABLE_HALO.get())) {
            double exponent = luck >= 0.0D
                    ? 1.0D / (1.0D + luck / 20.0D)
                    : 1.0D + Math.abs(luck) / 20.0D;
            double weighted = Math.pow(attacker.getRandom().nextDouble(), exponent);
            double multiplier = Math.max(0.60D, Math.min(2.0D, 0.60D + 1.40D * weighted));
            damage *= multiplier;
        }
        if (isEquipped(attacker, ModItems.KABBALAH_TREE.get())
                || KingdomComeOwnerTracker.isOwnedBy(target, attacker)) {
            damage *= 1.50D;
        }
        return damage;
    }

    private static boolean isEquipped(ServerPlayer player, Item item) {
        return CurioEquipmentHelper.isEquippedInSlot(player, item, RedemptionCharmItem.SLOT_ID);
    }
}
