package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.worldgen.DivinePortalShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DivinePortalEvents {
    public static final ResourceKey<Level> DIVINE_REALM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(until_eternity.MODID, "divine_realm"));
    private static final Map<UUID, Contact> CONTACTS = new HashMap<>();

    private DivinePortalEvents() {}

    @SubscribeEvent
    public static void onUseFinalKey(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(ModItems.FINAL_KEY.get())
                || !event.getLevel().getBlockState(event.getPos()).is(ModBlocks.DIVINE_CALCITE.get())) return;

        if (!(event.getLevel() instanceof ServerLevel level)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos inside = event.getPos().offset(dx, dy, dz);
                    var shape = DivinePortalShape.findEmpty(level, inside);
                    if (shape.isEmpty()) continue;
                    shape.get().createPortalBlocks();
                    level.playSound(null, event.getPos(), SoundEvents.PORTAL_TRIGGER,
                            SoundSource.BLOCKS, 0.8F, 1.3F);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    public static void entityInside(Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel source)) return;
        ResourceKey<Level> target = destinationFor(source.dimension());
        UUID id = entity.getUUID();
        if (target == null || entity.isPassenger() || entity.isVehicle()
                || !entity.canChangeDimensions()) {
            CONTACTS.remove(id);
            return;
        }
        if (entity.isOnPortalCooldown()) {
            entity.setPortalCooldown();
            CONTACTS.remove(id);
            return;
        }
        Contact contact = CONTACTS.get(id);
        if (contact == null || !contact.dimension.equals(source.dimension())) {
            contact = new Contact(source.dimension(), new DivinePortalTimer(pos));
            CONTACTS.put(id, contact);
        }
        if (contact.timer.touch(pos, source.getGameTime()) < entity.getPortalWaitTime()) return;
        ServerLevel destination = source.getServer().getLevel(target);
        if (destination == null) return;
        BlockPos entry = contact.timer.entrance();
        CONTACTS.remove(id);
        entity.setPortalCooldown();
        entity.changeDimension(destination, new DivinePortalTeleporter(destination, entry));
    }

    private static ResourceKey<Level> destinationFor(ResourceKey<Level> source) {
        if (Level.OVERWORLD.equals(source)) return DIVINE_REALM;
        if (DIVINE_REALM.equals(source)) return Level.OVERWORLD;
        return null;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<UUID, Contact>> iterator = CONTACTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Contact contact = iterator.next().getValue();
            ServerLevel level = server.getLevel(contact.dimension);
            if (level == null || contact.timer.decayIfUntouched(level.getGameTime())) iterator.remove();
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) CONTACTS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CONTACTS.clear();
    }

    private record Contact(ResourceKey<Level> dimension, DivinePortalTimer timer) {}
}
