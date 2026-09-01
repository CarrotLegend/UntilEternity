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
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Comparator;
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

    /** Search for an existing chaos portal near pos using the vanilla POI path. */
    public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos pos, WorldBorder border) {
        PoiManager poiManager = this.level.getPoiManager();
        poiManager.ensureLoadedAndValid(this.level, pos, SEARCH_RADIUS);
        Optional<PoiRecord> portal = poiManager.getInSquare(
                        holder -> holder.is(ModPoiTypes.CHAOS_PORTAL_KEY),
                        pos,
                        SEARCH_RADIUS,
                        PoiManager.Occupancy.ANY)
                .filter(record -> border.isWithinBounds(record.getPos()))
                .sorted(Comparator.<PoiRecord>comparingDouble(
                                record -> record.getPos().distSqr(pos))
                        .thenComparingInt(record -> record.getPos().getY()))
                .filter(record -> {
                    BlockState state = this.level.getBlockState(record.getPos());
                    return state.is(ModBlocks.CHAOS_PORTAL.get())
                            && state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
                })
                .findFirst();

        return portal.map(record -> {
            BlockPos portalPos = record.getPos();
            this.level.getChunkSource().addRegionTicket(
                    TicketType.PORTAL, new ChunkPos(portalPos), TICKET_RADIUS, portalPos);
            BlockState state = this.level.getBlockState(portalPos);
            return BlockUtil.getLargestRectangleAround(
                    portalPos,
                    state.getValue(BlockStateProperties.HORIZONTAL_AXIS),
                    21, Direction.Axis.Y, 21,
                    candidate -> this.level.getBlockState(candidate) == state);
        });
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
