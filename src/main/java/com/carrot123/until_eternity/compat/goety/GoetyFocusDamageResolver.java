package com.carrot123.until_eternity.compat.goety;

import com.Polarice3.Goety.utils.OwnedDamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public final class GoetyFocusDamageResolver {
    private GoetyFocusDamageResolver() {
    }

    public static ServerPlayer resolveCaster(
            ServerLevel level,
            DamageSource source
    ) {
        MinecraftServer server = level.getServer();
        ServerPlayer markedCaster = resolveMarked(
                server, source.getDirectEntity());
        if (markedCaster != null) {
            return markedCaster;
        }
        markedCaster = resolveMarked(server, source.getEntity());
        if (markedCaster != null) {
            return markedCaster;
        }

        Optional<UUID> currentCaster =
                GoetyFocusCastContext.currentCasterUuid();
        if (currentCaster.isEmpty()) {
            return null;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(
                currentCaster.get());
        if (player == null) {
            return null;
        }

        Entity direct = source.getDirectEntity();
        Entity causing = source.getEntity();
        if (direct == player || causing == player) {
            return player;
        }
        if (hasMatchingMarker(direct, player.getUUID())
                || hasMatchingMarker(causing, player.getUUID())) {
            return player;
        }
        if (isForeignLivingEntity(direct, player)
                || isForeignLivingEntity(causing, player)) {
            return null;
        }
        if (source instanceof OwnedDamageSource owned
                && owned.getOwner() == player) {
            return player;
        }
        return null;
    }

    private static ServerPlayer resolveMarked(
            MinecraftServer server,
            Entity entity
    ) {
        return GoetyFocusDamageMarker.getCasterUuid(entity)
                .map(server.getPlayerList()::getPlayer)
                .orElse(null);
    }

    private static boolean hasMatchingMarker(
            Entity entity,
            UUID casterUuid
    ) {
        return GoetyFocusDamageMarker.getCasterUuid(entity)
                .filter(casterUuid::equals)
                .isPresent();
    }

    private static boolean isForeignLivingEntity(
            Entity entity,
            ServerPlayer caster
    ) {
        return entity instanceof LivingEntity && entity != caster;
    }
}
