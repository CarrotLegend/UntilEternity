package com.carrot123.until_eternity.client.portal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public final class PortalVisualTracker {
    private static PortalVisualType currentType = PortalVisualType.NONE;
    private static boolean activationPending;

    private PortalVisualTracker() {
    }

    public static void markIfLocalPlayer(Entity entity, PortalVisualType type) {
        if (entity instanceof Player player && player.isLocalPlayer()) {
            mark(type);
        }
    }

    public static void resetIfLocalPlayer(Entity entity) {
        if (entity instanceof Player player && player.isLocalPlayer()) {
            reset();
        }
    }

    public static PortalVisualType currentType() {
        return currentType;
    }

    public static void updateFromVanillaEffect(float currentIntensity, float previousIntensity) {
        if (currentType == PortalVisualType.NONE) {
            return;
        }
        if (currentIntensity > 0.0F || previousIntensity > 0.0F) {
            activationPending = false;
            return;
        }
        if (activationPending) {
            activationPending = false;
            return;
        }
        reset();
    }

    public static void reset() {
        currentType = PortalVisualType.NONE;
        activationPending = false;
    }

    static void mark(PortalVisualType type) {
        currentType = Objects.requireNonNull(type);
        activationPending = type != PortalVisualType.NONE;
    }
}
