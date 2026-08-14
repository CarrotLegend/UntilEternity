package com.carrot123.until_eternity.compat.eeeabsmobs;

import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.worldgen.ImmortalDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ImmortalPortalHandler {
    private static final Map<UUID, PortalContact> CONTACTS =
            new HashMap<>();

    private ImmortalPortalHandler() {
    }

    public static void entityInside(Level level,
                                    BlockPos portalPos,
                                    Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ResourceKey<Level> destinationKey = destinationFor(
                serverLevel.dimension());
        UUID entityId = entity.getUUID();
        if (destinationKey == null
                || entity.isPassenger()
                || entity.isVehicle()
                || !entity.canChangeDimensions()) {
            CONTACTS.remove(entityId);
            return;
        }

        if (entity.isOnPortalCooldown()) {
            entity.setPortalCooldown();
            CONTACTS.remove(entityId);
            return;
        }

        PortalContact contact = CONTACTS.get(entityId);
        if (contact == null
                || !contact.dimension().equals(serverLevel.dimension())) {
            contact = new PortalContact(
                    serverLevel.dimension(),
                    new ImmortalPortalTimer(portalPos));
            CONTACTS.put(entityId, contact);
        }

        int portalTime = contact.timer().touch(
                portalPos, serverLevel.getGameTime());
        if (portalTime < entity.getPortalWaitTime()) {
            return;
        }

        ServerLevel destination = serverLevel.getServer()
                .getLevel(destinationKey);
        if (destination == null) {
            return;
        }

        BlockPos entrancePos = contact.timer().entrancePos();
        CONTACTS.remove(entityId);
        entity.setPortalCooldown();
        entity.changeDimension(destination,
                new ImmortalPortalTeleporter(
                        destination, entrancePos));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<UUID, PortalContact>> iterator =
                CONTACTS.entrySet().iterator();
        while (iterator.hasNext()) {
            PortalContact contact = iterator.next().getValue();
            ServerLevel level = server.getLevel(contact.dimension());
            if (level == null
                    || contact.timer().decayIfUntouched(
                            level.getGameTime())) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            CONTACTS.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CONTACTS.clear();
    }

    static ResourceKey<Level> destinationFor(ResourceKey<Level> source) {
        if (source.equals(Level.OVERWORLD)) {
            return ImmortalDimensions.IMMORTAL_DIMENSION;
        }
        if (source.equals(ImmortalDimensions.IMMORTAL_DIMENSION)) {
            return Level.OVERWORLD;
        }
        return null;
    }

    private record PortalContact(ResourceKey<Level> dimension,
                                 ImmortalPortalTimer timer) {
    }
}
