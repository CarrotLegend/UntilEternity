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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 完整复制原版 Entity.handleInsidePortal + handleNetherPortal 全部逻辑。
 * 不使用反射，不依赖原版字段。唯一区别：目标维度 chaos_realm。
 */
@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChaosPortalEvents {

    /** 对应 Entity.portalTime */
    private static final Map<UUID, Integer> portalTime = new ConcurrentHashMap<>();
    /** 对应 Entity.portalEntrancePos */
    private static final Map<UUID, BlockPos> portalEntrancePos = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.level().isClientSide)
            return;

        Player player = event.player;
        ServerLevel currentLevel = (ServerLevel) player.level();
        UUID uuid = player.getUUID();
        BlockPos pos = player.blockPosition();

        // 检测玩家是否在 chaos_portal 方块中（照搬 checkInsideBlocks 的检测逻辑）
        boolean inChaosPortal = currentLevel.getBlockState(pos).is(ModBlocks.CHAOS_PORTAL.get())
                || currentLevel.getBlockState(pos.above()).is(ModBlocks.CHAOS_PORTAL.get());

        if (inChaosPortal && player.canChangeDimensions()) {

            // ═════════════════════════════════════════════════════════
            //  照搬 Entity.handleInsidePortal(BlockPos)
            // ═════════════════════════════════════════════════════════
            if (player.isOnPortalCooldown()) {
                // 冷却中：重置冷却，阻止 portalTime 推进
                player.setPortalCooldown();
            } else {
                // 不在冷却：记录入口位置（用于 findDimensionEntryPoint 定位源传送门）
                BlockPos prev = portalEntrancePos.get(uuid);
                if (prev == null || !pos.equals(prev)) {
                    portalEntrancePos.put(uuid, pos.immutable());
                }
            }

            // ═════════════════════════════════════════════════════════
            //  照搬 Entity.handleNetherPortal – if (this.isInsidePortal) 分支
            // ═════════════════════════════════════════════════════════
            int waitTime = player.getAbilities().invulnerable ? 1 : 80;
            int t = portalTime.getOrDefault(uuid, 0);

            if (!player.isOnPortalCooldown() && !player.isPassenger()) {
                t++;
                portalTime.put(uuid, t);

                if (t >= waitTime) {
                    portalTime.put(uuid, waitTime);
                    player.setPortalCooldown();
                    changeDimension(player, currentLevel,
                            portalEntrancePos.getOrDefault(uuid, pos));
                }
            }

        } else {
            // ═════════════════════════════════════════════════════════
            //  照搬 Entity.handleNetherPortal – else 分支 (离开后衰减)
            // ═════════════════════════════════════════════════════════
            int t = portalTime.getOrDefault(uuid, 0);
            if (t > 0) {
                t = Math.max(0, t - 4);
                portalTime.put(uuid, t);
            }
            if (t == 0) {
                portalTime.remove(uuid);
                portalEntrancePos.remove(uuid);
            }
        }
        // processPortalCooldown() 由原版 Entity.tick() 自动执行 — 此处不重复
    }

    /* ── 照搬 changeDimension + findDimensionEntryPoint ─────────── */

    private static void changeDimension(Player player, ServerLevel from, BlockPos entrancePos) {
        ResourceKey<Level> destDim = from.dimension() == PortalForcer.CHAOS_REALM
                ? Level.OVERWORLD : PortalForcer.CHAOS_REALM;

        ServerLevel to = from.getServer().getLevel(destDim);
        if (to == null) return;

        WorldBorder border = to.getWorldBorder();
        double scale = net.minecraft.world.level.dimension.DimensionType.getTeleportationScale(
                from.dimensionType(), to.dimensionType());
        BlockPos scaled = border.clampToBounds(
                player.getX() * scale, player.getY(), player.getZ() * scale);

        PortalForcer forcer = new PortalForcer(to);
        Optional<BlockUtil.FoundRectangle> destRect = forcer.findPortalAround(scaled, border);

        if (destRect.isEmpty()) {
            Direction.Axis axis = getPortalAxisAt(from, entrancePos);
            destRect = forcer.createPortal(scaled, axis);
        }

        destRect.ifPresent(rect -> {
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

    /* ── 框架破坏清理 ────────────────────────────────────────── */

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
