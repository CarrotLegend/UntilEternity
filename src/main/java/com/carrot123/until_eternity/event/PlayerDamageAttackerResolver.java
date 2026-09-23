package com.carrot123.until_eternity.event;

import com.Polarice3.Goety.utils.OwnedDamageSource;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.Projectile;

public final class PlayerDamageAttackerResolver {
    private static final int MAX_OWNER_DEPTH = 8;

    private PlayerDamageAttackerResolver() {
    }

    @Nullable
    public static ServerPlayer resolve(DamageSource source) {
        Set<Entity> visited = Collections.newSetFromMap(
                new IdentityHashMap<>());
        ServerPlayer player = resolveOwner(source.getEntity(), visited, 0);
        if (player != null) {
            return player;
        }
        if (source instanceof OwnedDamageSource ownedDamageSource) {
            player = resolveOwner(ownedDamageSource.getOwner(), visited, 0);
            if (player != null) {
                return player;
            }
        }
        return resolveOwner(source.getDirectEntity(), visited, 0);
    }

    @Nullable
    private static ServerPlayer resolveOwner(
            @Nullable Entity entity,
            Set<Entity> visited,
            int depth
    ) {
        if (entity == null
                || depth > MAX_OWNER_DEPTH
                || !visited.add(entity)) {
            return null;
        }
        if (entity instanceof ServerPlayer player) {
            return player;
        }
        if (entity instanceof Projectile projectile) {
            ServerPlayer player = resolveOwner(
                    projectile.getOwner(), visited, depth + 1);
            if (player != null) {
                return player;
            }
        }
        if (entity instanceof OwnableEntity ownableEntity) {
            return resolveOwner(ownableEntity.getOwner(), visited, depth + 1);
        }
        return null;
    }
}
