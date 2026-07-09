package com.carrot123.until_eternity.event;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles chaos portal teleport using the EXACT same logic as vanilla
 * Entity.handleInsidePortal() + Entity.handleNetherPortal().
 *
 * Per-player state mirrors Entity fields:
 *   portalTime      — accumulated time inside portal
 *   portalEntrance  — the BlockPos where portal was first entered
 */
@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChaosPortalEvents {

    /** Per-player timer — mirrors Entity.portalTime */
    private static final Map<UUID, Integer> portalTime = new ConcurrentHashMap<>();
    /** Per-player entrance position — mirrors Entity.portalEntrancePos */
    private static final Map<UUID, BlockPos> portalEntrance = new ConcurrentHashMap<>();

    // ── handleInsidePortal equivalent ──────────────────────────────

    /**
     * Called each tick while the player is inside a chaos_portal block.
     * Mirrors Entity.handleInsidePortal(BlockPos).
     */
    private static void handleInsidePortal(Player player, BlockPos pos) {
        if (player.isOnPortalCooldown()) {
            // While on cooldown, reset it each tick — prevents timer from running.
            // This is exactly what vanilla does: setPortalCooldown() resets
            // the cooldown to getDimensionChangingDelay() (= 10 for players).
            player.setPortalCooldown();
        } else {
            if (!pos.equals(portalEntrance.get(player.getUUID()))) {
                portalEntrance.put(player.getUUID(), pos.immutable());
            }
        }
    }

    // ── handleNetherPortal equivalent ──────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        ServerLevel currentLevel = (ServerLevel) player.level();
        UUID uuid = player.getUUID();

        // Check if player is inside a chaos_portal block
        BlockPos pos = player.blockPosition();
        boolean inPortal = currentLevel.getBlockState(pos).is(ModBlocks.CHAOS_PORTAL.get())
                || currentLevel.getBlockState(pos.above()).is(ModBlocks.CHAOS_PORTAL.get());

        if (inPortal && player.canChangeDimensions()) {
            // handleInsidePortal equivalent
            handleInsidePortal(player, pos);

            // handleNetherPortal: inside portal branch
            int waitTime = player.getAbilities().invulnerable ? 1 : 80;
            int t = portalTime.getOrDefault(uuid, 0);

            if (!player.isOnPortalCooldown() && !player.isPassenger()) {
                t++;
                portalTime.put(uuid, t);

                if (t >= waitTime) {
                    portalTime.put(uuid, waitTime);
                    player.setPortalCooldown();
                    changeDimension(player, currentLevel, pos);
                }
            }
        } else {
            // handleNetherPortal: outside portal branch — decay timer
            int t = portalTime.getOrDefault(uuid, 0);
            if (t > 0) {
                t = Math.max(0, t - 4);
                portalTime.put(uuid, t);
            }
            if (t == 0) {
                portalTime.remove(uuid);
                portalEntrance.remove(uuid);
            }
        }
    }

    // ── changeDimension equivalent ────────────────────────────────

    private static void changeDimension(Player player, ServerLevel from, BlockPos fromPos) {
        ResourceKey<Level> destDim = from.dimension() == PortalForcer.CHAOS_REALM
                ? Level.OVERWORLD : PortalForcer.CHAOS_REALM;

        ServerLevel to = from.getServer().getLevel(destDim);
        if (to == null) return;

        // findDimensionEntryPoint equivalent
        WorldBorder border = to.getWorldBorder();
        double scale = net.minecraft.world.level.dimension.DimensionType.getTeleportationScale(
                from.dimensionType(), to.dimensionType());
        BlockPos scaled = border.clampToBounds(
                player.getX() * scale, player.getY(), player.getZ() * scale);

        PortalForcer forcer = new PortalForcer(to);
        Optional<BlockUtil.FoundRectangle> destRect = forcer.findPortalAround(scaled, border);

        if (destRect.isEmpty()) {
            Direction.Axis axis = getPortalAxisAt(from, fromPos);
            destRect = forcer.createPortal(scaled, axis);
        }

        destRect.ifPresent(rect -> {
            // getRelativePortalPosition + createPortalInfo equivalent
            BlockPos entrance = portalEntrance.getOrDefault(player.getUUID(), fromPos);
            BlockState sourceState = from.getBlockState(entrance);
            Direction.Axis sourceAxis = Direction.Axis.X;
            Vec3 relativePos = new Vec3(0.5, 0.0, 0.0);

            if (sourceState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                sourceAxis = sourceState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                BlockUtil.FoundRectangle sourceRect = BlockUtil.getLargestRectangleAround(
                        entrance, sourceAxis, 21, Direction.Axis.Y, 21,
                        p -> from.getBlockState(p) == sourceState);
                relativePos = PortalShape.getRelativePosition(sourceRect, sourceAxis,
                        player.position(), player.getDimensions(player.getPose()));
            }

            PortalInfo info = PortalShape.createPortalInfo(to, rect, sourceAxis,
                    relativePos, player, player.getDeltaMovement(),
                    player.getYRot(), player.getXRot());

            if (player instanceof ServerPlayer sp) {
                sp.teleportTo(to, info.pos.x, info.pos.y, info.pos.z,
                        info.yRot, info.xRot);
                sp.setDeltaMovement(info.speed);
            }

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

    // ── Frame break cleanup ───────────────────────────────────────

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        BlockState broken = event.getState();
        if (!broken.getBlock().getDescriptionId().contains("deepslate")) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(dir);
            BlockState ns = level.getBlockState(neighbor);
            if (ns.is(ModBlocks.CHAOS_PORTAL.get())
                    && ns.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                Direction.Axis axis = ns.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                Direction rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
                BlockUtil.FoundRectangle rect = BlockUtil.getLargestRectangleAround(
                        neighbor, axis, 21, Direction.Axis.Y, 21,
                        p -> level.getBlockState(p).is(ModBlocks.CHAOS_PORTAL.get()));
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
    }
}
