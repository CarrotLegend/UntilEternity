package com.carrot123.until_eternity.effect;

import com.carrot123.until_eternity.registry.ModMobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class KingdomComeOwnerTracker {
    private static final String OWNER_KEY = "until_eternity:kingdom_come_owner";

    private KingdomComeOwnerTracker() {
    }

    public static void remember(LivingEntity target, Player owner) {
        target.getPersistentData().putUUID(OWNER_KEY, owner.getUUID());
    }

    public static boolean isOwnedBy(LivingEntity target, Player player) {
        if (!target.hasEffect(ModMobEffects.KINGDOM_COME.get())) {
            return false;
        }
        CompoundTag data = target.getPersistentData();
        return data.hasUUID(OWNER_KEY)
                && data.getUUID(OWNER_KEY).equals(player.getUUID());
    }

    public static void clear(LivingEntity target) {
        target.getPersistentData().remove(OWNER_KEY);
    }
}
