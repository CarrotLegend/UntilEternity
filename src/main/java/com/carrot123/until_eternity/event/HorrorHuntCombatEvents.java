package com.carrot123.until_eternity.event;

import com.Polarice3.Goety.utils.OwnedDamageSource;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HorrorHuntCombatEvents {
    static final String NEXT_PROC_TAG =
            "until_eternity:horror_hunt_next_proc";
    private static final int MAX_OWNER_DEPTH = 8;

    private HorrorHuntCombatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        float amount = event.getAmount();
        if (event.getEntity().level().isClientSide
                || amount <= 0.0F
                || !Float.isFinite(amount)) {
            return;
        }

        ServerPlayer attacker = resolvePlayerAttacker(event.getSource());
        if (attacker == null || attacker == event.getEntity()) {
            return;
        }
        if (!CuriosApi.getCuriosInventory(attacker)
                .map(handler -> handler.isEquipped(
                        ModItems.HORROR_HUNT.get()))
                .orElse(false)) {
            return;
        }

        long gameTime = attacker.server.overworld().getGameTime();
        long nextProcGameTime = readNextProcGameTime(attacker);
        if (!HorrorHuntDamageLogic.isReady(gameTime, nextProcGameTime)) {
            return;
        }

        float amplified = HorrorHuntDamageLogic.amplifiedDamage(amount);
        if (!Float.isFinite(amplified)) {
            return;
        }

        writeNextProcGameTime(attacker,
                HorrorHuntDamageLogic.nextProcGameTime(gameTime));
        event.setAmount(amplified);
    }

    @Nullable
    static ServerPlayer resolvePlayerAttacker(DamageSource source) {
        Set<Entity> visited = Collections.newSetFromMap(
                new IdentityHashMap<>());

        ServerPlayer player = resolvePlayerOwner(
                source.getEntity(), visited, 0);
        if (player != null) {
            return player;
        }
        if (source instanceof OwnedDamageSource ownedDamageSource) {
            player = resolvePlayerOwner(
                    ownedDamageSource.getOwner(), visited, 0);
            if (player != null) {
                return player;
            }
        }
        return resolvePlayerOwner(source.getDirectEntity(), visited, 0);
    }

    @Nullable
    private static ServerPlayer resolvePlayerOwner(
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
            ServerPlayer player = resolvePlayerOwner(
                    projectile.getOwner(), visited, depth + 1);
            if (player != null) {
                return player;
            }
        }
        if (entity instanceof OwnableEntity ownableEntity) {
            return resolvePlayerOwner(
                    ownableEntity.getOwner(), visited, depth + 1);
        }
        return null;
    }

    private static long readNextProcGameTime(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            return 0L;
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG)
                .getLong(NEXT_PROC_TAG);
    }

    private static void writeNextProcGameTime(
            Player player,
            long nextProcGameTime
    ) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted;
        if (root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        } else {
            persisted = new CompoundTag();
            root.put(Player.PERSISTED_NBT_TAG, persisted);
        }
        persisted.putLong(NEXT_PROC_TAG, nextProcGameTime);
    }
}
