package com.carrot123.until_eternity.compat.goety;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public final class GoetyFocusDamageMarker {
    public static final String FOCUS_DAMAGE_SOURCE_TAG =
            "until_eternity:focus_spell_damage";
    public static final String FOCUS_CASTER_TAG =
            "until_eternity:focus_caster";

    private GoetyFocusDamageMarker() {
    }

    public static boolean mark(Entity entity, UUID casterUuid) {
        if (!canMark(entity) || casterUuid == null) {
            return false;
        }
        CompoundTag data = entity.getPersistentData();
        data.putBoolean(FOCUS_DAMAGE_SOURCE_TAG, true);
        data.putUUID(FOCUS_CASTER_TAG, casterUuid);
        return true;
    }

    public static boolean canMark(Entity entity) {
        return entity != null && !(entity instanceof LivingEntity);
    }

    public static Optional<UUID> getCasterUuid(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean(FOCUS_DAMAGE_SOURCE_TAG)
                || !data.hasUUID(FOCUS_CASTER_TAG)) {
            return Optional.empty();
        }
        return Optional.of(data.getUUID(FOCUS_CASTER_TAG));
    }

    public static boolean isMarked(Entity entity) {
        return getCasterUuid(entity).isPresent();
    }
}
