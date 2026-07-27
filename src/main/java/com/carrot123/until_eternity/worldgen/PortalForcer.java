package com.carrot123.until_eternity.worldgen;

import com.carrot123.until_eternity.block.ChaosPortalBlock;
import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.Optional;

public class PortalForcer {
    public static final ResourceKey<Level> CHAOS_REALM =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(until_eternity.MODID, "chaos_realm"));

    private static final int TICKET_RADIUS = 3;
    private static final int SEARCH_RADIUS = 128;
    private static final int CREATE_RADIUS = 16;
    private final ServerLevel level;

    public PortalForcer(ServerLevel level) {
        this.level = level;
    }

    /**
     * Search for an existing chaos portal near pos.
     * Synchronously loads chunks then does a 3D scan (XZ spiral, Y outward from center).
     */
    public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos pos, WorldBorder border) {
        int searchRadius = 128;
        // 同步加载 chunk（强制等待 FULL 状态）
        int cr = (searchRadius >> 4) + 1;
        ChunkPos centerCp = new ChunkPos(pos);
        for (int dx = -cr; dx <= cr; dx++) {
            for (int dz = -cr; dz <= cr; dz++) {
                this.level.getChunkSource().getChunk(
                        centerCp.x + dx, centerCp.z + dz, ChunkStatus.FULL, true);
            }
        }
        // 3D 搜索：XZ 螺旋，每处 Y 从中心向外扩展
        int yCenter = pos.getY();
        int yMin = Math.max(this.level.getMinBuildHeight(), yCenter - 64);
        int yMax = Math.min(this.level.getMaxBuildHeight() - 1, yCenter + 64);
        int yRange = Math.min(64, Math.max(yCenter - yMin, yMax - yCenter));
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (BlockPos.MutableBlockPos xz : BlockPos.spiralAround(
                new BlockPos(pos.getX(), yCenter, pos.getZ()),
                searchRadius, Direction.EAST, Direction.SOUTH)) {
            if (!border.isWithinBounds(xz)) continue;
            // Y 从中心向外搜索（dy=0, -1, +1, -2, +2, ...）
            for (int dy = 0; dy <= yRange; dy++) {
                for (int sign = -1; sign <= 1; sign += 2) {
                    int y = yCenter + dy * sign;
                    if (dy == 0 && sign == 1) continue; // dy=0 只检查一次
                    if (y < yMin || y > yMax) continue;
                    mut.set(xz.getX(), y, xz.getZ());
                    Optional<BlockUtil.FoundRectangle> found = tryExpandPortal(mut);
                    if (found.isPresent()) return found;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<BlockUtil.FoundRectangle> tryExpandPortal(BlockPos.MutableBlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (state.is(ModBlocks.CHAOS_PORTAL.get()) && state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            this.level.getChunkSource().addRegionTicket(
                    TicketType.PORTAL, new ChunkPos(pos), TICKET_RADIUS, pos);
            return Optional.of(BlockUtil.getLargestRectangleAround(
                    pos.immutable(),
                    state.getValue(BlockStateProperties.HORIZONTAL_AXIS),
                    21, Direction.Axis.Y, 21,
                    p -> this.level.getBlockState(p) == state));
        }
        return Optional.empty();
    }

    /**
     * Create a new chaos portal at the best location near pos.
     * Builds a deepslate frame and fills with chaos_portal blocks.
     */
    public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos pos, Direction.Axis axis) {
        Direction dir = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double bestDist = -1.0;
        BlockPos bestPos = null;
        double fallbackDist = -1.0;
        BlockPos fallbackPos = null;
        WorldBorder border = this.level.getWorldBorder();
        int maxY = Math.min(this.level.getMaxBuildHeight(),
                this.level.getMinBuildHeight() + this.level.getLogicalHeight()) - 1;
        BlockPos.MutableBlockPos mut = pos.mutable();

        // Spiral search for a suitable location
        for (BlockPos.MutableBlockPos candidate : BlockPos.spiralAround(pos, CREATE_RADIUS, Direction.EAST, Direction.SOUTH)) {
            int topY = Math.min(maxY,
                    this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, candidate.getX(), candidate.getZ()));
            if (!border.isWithinBounds(candidate) || !border.isWithinBounds(candidate.move(dir, 1))) continue;
            candidate.move(dir.getOpposite(), 1);

            for (int y = topY; y >= this.level.getMinBuildHeight(); y--) {
                candidate.setY(y);
                if (canPortalReplaceBlock(candidate)) {
                    int startY = y;
                    while (y > this.level.getMinBuildHeight() && canPortalReplaceBlock(candidate.move(Direction.DOWN))) {
                        y--;
                    }
                    if (y + 4 <= maxY) {
                        int drop = startY - y;
                        if (drop <= 0 || drop >= 3) {
                            candidate.setY(y);
                            if (canHostFrame(candidate, mut, dir, 0)) {
                                double dist = pos.distSqr(candidate);
                                if (canHostFrame(candidate, mut, dir, -1)
                                        && canHostFrame(candidate, mut, dir, 1)
                                        && (bestDist == -1.0 || bestDist > dist)) {
                                    bestDist = dist;
                                    bestPos = candidate.immutable();
                                }
                                if (bestDist == -1.0 && (fallbackDist == -1.0 || fallbackDist > dist)) {
                                    fallbackDist = dist;
                                    fallbackPos = candidate.immutable();
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestDist == -1.0 && fallbackDist != -1.0) {
            bestPos = fallbackPos;
            bestDist = fallbackDist;
        }

        // If no position found, force one at reasonable height
        if (bestDist == -1.0) {
            int clampedY = Mth.clamp(pos.getY(), Math.max(this.level.getMinBuildHeight() + 1, 70), maxY - 9);
            bestPos = new BlockPos(pos.getX(), clampedY, pos.getZ()).immutable();
            Direction clockwise = dir.getClockWise();
            if (!border.isWithinBounds(bestPos)) {
                return Optional.empty();
            }
            // Clear area for the portal
            for (int x = -1; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    for (int y = -1; y < 3; y++) {
                        BlockState fill = y < 0 ? Blocks.AIR.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        mut.setWithOffset(bestPos,
                                z * dir.getStepX() + x * clockwise.getStepX(), y,
                                z * dir.getStepZ() + x * clockwise.getStepZ());
                        this.level.setBlockAndUpdate(mut, fill);
                    }
                }
            }
        }

        // Build deepslate frame (4 wide x 5 tall = 2x3 interior)
        BlockState frameBlock = findDeepslateBlock();
        for (int x = -1; x < 3; x++) {
            for (int y = -1; y < 4; y++) {
                if (x == -1 || x == 2 || y == -1 || y == 3) {
                    mut.setWithOffset(bestPos,
                            x * dir.getStepX(), y,
                            x * dir.getStepZ());
                    this.level.setBlock(mut, frameBlock, 3);
                }
            }
        }

        // Fill interior with chaos_portal blocks (2x3)
        BlockState portalState = ModBlocks.CHAOS_PORTAL.get().defaultBlockState()
                .setValue(ChaosPortalBlock.AXIS, axis);
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                mut.setWithOffset(bestPos,
                        x * dir.getStepX(), y,
                        x * dir.getStepZ());
                this.level.setBlock(mut, portalState, 18);
            }
        }

        return Optional.of(new BlockUtil.FoundRectangle(bestPos.immutable(), 2, 3));
    }

    private boolean canPortalReplaceBlock(BlockPos.MutableBlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private boolean canHostFrame(BlockPos pos, BlockPos.MutableBlockPos mut, Direction dir, int offset) {
        Direction clockwise = dir.getClockWise();
        for (int x = -1; x < 3; x++) {
            for (int y = -1; y < 4; y++) {
                mut.setWithOffset(pos,
                        dir.getStepX() * x + clockwise.getStepX() * offset, y,
                        dir.getStepZ() * x + clockwise.getStepZ() * offset);
                if (y < 0 && !this.level.getBlockState(mut).isSolid()) {
                    return false;
                }
                if (y >= 0 && !canPortalReplaceBlock(mut)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Find an available deepslate variant to use as frame block. */
    private BlockState findDeepslateBlock() {
        return Blocks.DEEPSLATE.defaultBlockState();
    }
}
