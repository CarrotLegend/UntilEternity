package com.carrot123.until_eternity.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public final class VoidCorrosionSourceTracker {
    static final String SOURCE_KEY =
            "until_eternity:void_corrosion_source";

    private VoidCorrosionSourceTracker() {
    }

    public static void remember(LivingEntity target, Player source) {
        write(target.getPersistentData(), source.getUUID());
    }

    @Nullable
    public static ServerPlayer resolve(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return read(target.getPersistentData())
                .map(serverLevel.getServer().getPlayerList()::getPlayer)
                .orElse(null);
    }

    public static void clear(LivingEntity target) {
        clear(target.getPersistentData());
    }

    static void write(CompoundTag tag, UUID sourceId) {
        tag.putUUID(SOURCE_KEY, sourceId);
    }

    static Optional<UUID> read(CompoundTag tag) {
        if (!tag.hasUUID(SOURCE_KEY)) {
            return Optional.empty();
        }
        return Optional.of(tag.getUUID(SOURCE_KEY));
    }

    static void clear(CompoundTag tag) {
        tag.remove(SOURCE_KEY);
    }
}
