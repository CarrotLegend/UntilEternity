package com.carrot123.until_eternity.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EntityHurtAnimClientHandler {

    private EntityHurtAnimClientHandler() {
    }

    public static void handle(int entityId) {
        Minecraft minecraft =
                Minecraft.getInstance();

        ClientLevel level =
                minecraft.level;

        if (level == null) {
            return;
        }

        Entity entity =
                level.getEntity(entityId);

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        living.handleDamageEvent(
                living.damageSources().generic()
        );
    }
}