package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.worldgen.PortalForcer;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChaosPortalEvents {
    private static final Map<UUID, PortalContact> CONTACTS = new HashMap<>();

    private ChaosPortalEvents() {
    }

    public static void entityInside(Level level, BlockPos portalPos, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ResourceKey<Level> destinationKey = destinationFor(serverLevel.dimension());
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
        if (contact == null || !contact.dimension().equals(serverLevel.dimension())) {
            contact = new PortalContact(
                    serverLevel.dimension(), new ChaosPortalTimer(portalPos));
            CONTACTS.put(entityId, contact);
        }

        int portalTime = contact.timer().touch(portalPos, serverLevel.getGameTime());
        if (portalTime < entity.getPortalWaitTime()) {
            return;
        }

        ServerLevel destination = serverLevel.getServer().getLevel(destinationKey);
        if (destination == null) {
            return;
        }

        BlockPos entrancePos = contact.timer().entrancePos();
        CONTACTS.remove(entityId);
        entity.setPortalCooldown();
        entity.changeDimension(destination, new ChaosPortalTeleporter(destination, entrancePos));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<UUID, PortalContact>> iterator = CONTACTS.entrySet().iterator();
        while (iterator.hasNext()) {
            PortalContact contact = iterator.next().getValue();
            ServerLevel level = server.getLevel(contact.dimension());
            if (level == null || contact.timer().decayIfUntouched(level.getGameTime())) {
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
            return PortalForcer.CHAOS_REALM;
        }
        if (source.equals(PortalForcer.CHAOS_REALM)) {
            return Level.OVERWORLD;
        }
        return null;
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        BlockState broken = event.getState();
        if (!broken.getBlock().getDescriptionId().contains("deepslate")) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.is(ModBlocks.CHAOS_PORTAL.get())
                    && neighborState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                Direction.Axis axis = neighborState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                Direction rightDirection = axis == Direction.Axis.X
                        ? Direction.WEST
                        : Direction.SOUTH;
                BlockUtil.FoundRectangle rectangle = BlockUtil.getLargestRectangleAround(
                        neighbor,
                        axis,
                        21,
                        Direction.Axis.Y,
                        21,
                        candidate -> level.getBlockState(candidate).is(ModBlocks.CHAOS_PORTAL.get()));
                BlockPos.betweenClosed(
                                rectangle.minCorner,
                                rectangle.minCorner.relative(Direction.UP, rectangle.axis2Size - 1)
                                        .relative(rightDirection, rectangle.axis1Size - 1))
                        .forEach(portalPos -> {
                            if (level.getBlockState(portalPos).is(ModBlocks.CHAOS_PORTAL.get())) {
                                level.setBlock(portalPos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        });
            }
        }
    }

    private record PortalContact(ResourceKey<Level> dimension, ChaosPortalTimer timer) {
    }
}
