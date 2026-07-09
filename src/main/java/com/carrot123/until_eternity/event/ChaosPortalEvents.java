package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.block.ChaosPortalBlock;
import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.worldgen.PortalForcer;
import com.carrot123.until_eternity.worldgen.PortalShape;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChaosPortalEvents {

    /** Per-player portal timer, mirrors Entity.portalTime. */
    private static final Map<UUID, Integer> portalTimers = new ConcurrentHashMap<>();
    /** Per-player last portal position. */
    private static final Map<UUID, BlockPos> portalEntrancePos = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        ServerLevel currentLevel = (ServerLevel) player.level();
        UUID uuid = player.getUUID();
        int timer = portalTimers.getOrDefault(uuid, 0);

        // Check if player is standing inside a chaos_portal block
        BlockPos feetPos = player.blockPosition();
        BlockState feetState = currentLevel.getBlockState(feetPos);
        boolean isInChaosPortal = feetState.is(ModBlocks.CHAOS_PORTAL.get());

        // Also check head position for tall portals
        if (!isInChaosPortal) {
            BlockPos headPos = feetPos.above();
            BlockState headState = currentLevel.getBlockState(headPos);
            isInChaosPortal = headState.is(ModBlocks.CHAOS_PORTAL.get());
        }

        if (isInChaosPortal && player.canChangeDimensions()) {
            if (player.isOnPortalCooldown()) {
                // Reset timer while on cooldown
                portalTimers.put(uuid, 0);
                return;
            }

            int waitTime = player.getAbilities().invulnerable ? 1 : 80;
            timer++;
            portalTimers.put(uuid, timer);

            // Record entrance position
            if (!feetPos.equals(portalEntrancePos.get(uuid))) {
                portalEntrancePos.put(uuid, feetPos.immutable());
            }

            if (timer >= waitTime) {
                // Determine destination dimension
                ResourceKey<Level> destDim;
                if (currentLevel.dimension() == PortalForcer.CHAOS_REALM) {
                    destDim = Level.OVERWORLD;
                } else {
                    destDim = PortalForcer.CHAOS_REALM;
                }

                ServerLevel destLevel = currentLevel.getServer().getLevel(destDim);
                if (destLevel != null) {
                    portalTimers.put(uuid, waitTime);
                    player.setPortalCooldown();
                    teleportPlayer(player, currentLevel, destLevel, feetPos);
                }
            }
        } else {
            // Outside portal: timer decays
            if (timer > 0) {
                timer = Math.max(0, timer - 4);
                portalTimers.put(uuid, timer);
            }
            if (timer == 0) {
                portalTimers.remove(uuid);
                portalEntrancePos.remove(uuid);
            }
        }
    }

    private static void teleportPlayer(Player player, ServerLevel from, ServerLevel to, BlockPos portalPos) {
        WorldBorder border = to.getWorldBorder();
        double scale = net.minecraft.world.level.dimension.DimensionType.getTeleportationScale(
                from.dimensionType(), to.dimensionType());
        BlockPos scaledPos = border.clampToBounds(
                player.getX() * scale, player.getY(), player.getZ() * scale);

        PortalForcer forcer = new PortalForcer(to);

        // Find or create a portal at the destination
        Optional<BlockUtil.FoundRectangle> destRect = forcer.findPortalAround(scaledPos, border);
        if (destRect.isEmpty()) {
            Direction.Axis axis = getPortalAxisAt(from, portalPos);
            destRect = forcer.createPortal(scaledPos, axis);
        }

        destRect.ifPresent(rect -> {
            // Calculate relative position in source portal
            BlockPos entrancePos = portalEntrancePos.getOrDefault(player.getUUID(), portalPos);
            BlockState sourceState = from.getBlockState(entrancePos);
            Direction.Axis sourceAxis = Direction.Axis.X;
            Vec3 relativePos = new Vec3(0.5, 0.0, 0.0);

            if (sourceState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                sourceAxis = sourceState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                BlockUtil.FoundRectangle sourceRect = BlockUtil.getLargestRectangleAround(
                        entrancePos, sourceAxis, 21, Direction.Axis.Y, 21,
                        p -> from.getBlockState(p) == sourceState);
                relativePos = PortalShape.getRelativePosition(sourceRect, sourceAxis,
                        player.position(), player.getDimensions(player.getPose()));
            }

            PortalInfo info = PortalShape.createPortalInfo(to, rect, sourceAxis,
                    relativePos, player, player.getDeltaMovement(), player.getYRot(), player.getXRot());

            // Teleport
            if (player instanceof ServerPlayer sp) {
                sp.teleportTo(to, info.pos.x, info.pos.y, info.pos.z, info.yRot, info.xRot);
                sp.setDeltaMovement(info.speed);
            } else {
                player.changeDimension(to);
            }

            // Play travel sound
            to.playSound(null, BlockPos.containing(info.pos),
                    SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS,
                    0.25F, to.random.nextFloat() * 0.4F + 0.8F);
        });
    }

    private static Direction.Axis getPortalAxisAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        }
        return Direction.Axis.X;
    }

    /**
     * Clean up portal blocks when deepslate frame is broken.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        BlockState broken = event.getState();
        if (!broken.getBlock().getDescriptionId().contains("deepslate")) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();

        // Check all four horizontal neighbors for portal blocks
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.is(ModBlocks.CHAOS_PORTAL.get())
                    && neighborState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                // Remove connected portal blocks
                Direction.Axis axis = neighborState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                removeConnectedPortal(level, neighbor, axis);
            }
        }
    }

    private static void removeConnectedPortal(ServerLevel level, BlockPos start, Direction.Axis axis) {
        Direction rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        // Find the portal rectangle
        BlockUtil.FoundRectangle rect = BlockUtil.getLargestRectangleAround(
                start, axis, 21, Direction.Axis.Y, 21,
                p -> level.getBlockState(p).is(ModBlocks.CHAOS_PORTAL.get()));
        // Remove all portal blocks in the rectangle
        BlockPos.betweenClosed(rect.minCorner,
                rect.minCorner.relative(Direction.UP, rect.axis2Size - 1)
                        .relative(rightDir, rect.axis1Size - 1))
                .forEach(p -> {
                    if (level.getBlockState(p).is(ModBlocks.CHAOS_PORTAL.get())) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                });
    }
}
