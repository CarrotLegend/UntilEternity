package com.carrot123.until_eternity.tarot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public interface TarotSetEffect {
    default void onActivate(ServerPlayer player) {
    }

    default void onDeactivate(ServerPlayer player) {
    }

    default void onTick(ServerPlayer player) {
    }

    default void onDamage(ServerPlayer player, LivingDamageEvent event) {
    }
}
