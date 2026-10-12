package com.carrot123.until_eternity.worldgen;

import com.carrot123.until_eternity.block.DivinePortalBlock;
import com.carrot123.until_eternity.block.ModBlocks;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.util.Mth;

import java.util.Comparator;
import java.util.Optional;

public final class DivinePortalForcer {
    private final ServerLevel level;

    public DivinePortalForcer(ServerLevel level) {
        this.level = level;
    }

    public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos near, WorldBorder border) {
        PoiManager manager = level.getPoiManager();
        manager.ensureLoadedAndValid(level, near, 128);
        Optional<PoiRecord> result = manager.getInSquare(
                        holder -> holder.is(ModPoiTypes.DIVINE_PORTAL_KEY), near,
                        128, PoiManager.Occupancy.ANY)
                .filter(poi -> border.isWithinBounds(poi.getPos()))
                .filter(poi -> level.getBlockState(poi.getPos()).is(ModBlocks.DIVINE_PORTAL.get()))
                .sorted(Comparator.<PoiRecord>comparingDouble(poi -> poi.getPos().distSqr(near))
                        .thenComparingInt(poi -> poi.getPos().getY()))
                .findFirst();
        return result.map(poi -> {
            BlockPos position = poi.getPos();
            level.getChunkSource().addRegionTicket(TicketType.PORTAL,
                    new ChunkPos(position), 3, position);
            Direction.Axis axis = level.getBlockState(position).getValue(BlockStateProperties.HORIZONTAL_AXIS);
            return BlockUtil.getLargestRectangleAround(position, axis, 21, Direction.Axis.Y,
                    21, p -> level.getBlockState(p).is(ModBlocks.DIVINE_PORTAL.get())
                            && level.getBlockState(p).getValue(BlockStateProperties.HORIZONTAL_AXIS) == axis);
        });
    }

    public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos near, Direction.Axis axis) {
        WorldBorder border = level.getWorldBorder();
        BlockPos chosen = null;
        for (int radius = 0; radius <= 8 && chosen == null; radius++) {
            for (int dx = -radius; dx <= radius && chosen == null; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                    int x = near.getX() + dx;
                    int z = near.getZ() + dz;
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos bottomInside = new BlockPos(x, y, z);
                    if (canPlace(bottomInside, axis, border)) {
                        chosen = bottomInside;
                        break;
                    }
                }
            }
        }

        boolean fallback = chosen == null;
        if (fallback) {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near.getX(), near.getZ());
            int maxY = level.getMinBuildHeight() + level.getLogicalHeight() - 5;
            y = Mth.clamp(y, level.getMinBuildHeight() + 3, maxY);
            chosen = new BlockPos(near.getX(), y, near.getZ());
        }

        Direction widthDirection = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Direction normal = widthDirection.getClockWise();
        if (!border.isWithinBounds(chosen.relative(widthDirection, -2))
                || !border.isWithinBounds(chosen.relative(widthDirection, 3))) return Optional.empty();

        if (fallback) {
            for (int x = -2; x <= 3; x++) {
                for (int depth = -2; depth <= 2; depth++) {
                    BlockPos floor = chosen.relative(widthDirection, x).relative(normal, depth).below();
                    level.setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
                    for (int y = 0; y <= 3; y++) {
                        BlockPos air = floor.above(y + 1);
                        level.setBlock(air, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        BlockState frame = ModBlocks.DIVINE_CALCITE.get().defaultBlockState();
        BlockState portal = ModBlocks.DIVINE_PORTAL.get().defaultBlockState()
                .setValue(DivinePortalBlock.AXIS, axis);
        for (int x = -1; x <= 2; x++) {
            for (int y = -1; y <= 3; y++) {
                if (x != -1 && x != 2 && y != -1 && y != 3) continue;
                BlockPos framePos = chosen.relative(widthDirection, x).above(y);
                level.setBlock(framePos, frame, 3);
            }
        }
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                level.setBlock(chosen.relative(widthDirection, x).above(y), portal, 18);
            }
        }
        return Optional.of(new BlockUtil.FoundRectangle(chosen.immutable(), 2, 3));
    }

    private boolean canPlace(BlockPos inside, Direction.Axis axis, WorldBorder border) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMinBuildHeight() + level.getLogicalHeight() - 4;
        if (inside.getY() < minY || inside.getY() > maxY) return false;
        Direction widthDirection = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Direction normal = widthDirection.getClockWise();
        for (int x = -1; x <= 2; x++) {
            for (int y = -1; y <= 3; y++) {
                BlockPos p = inside.relative(widthDirection, x).above(y);
                if (!border.isWithinBounds(p)) return false;
                if (y == -1) {
                    if (!level.getBlockState(p).isSolid()) return false;
                } else if (!level.getBlockState(p).canBeReplaced()) {
                    return false;
                }
            }
        }
        for (int depth = -1; depth <= 1; depth += 2) {
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    BlockPos p = inside.relative(widthDirection, x).relative(normal, depth).above(y);
                    if (!level.getBlockState(p).canBeReplaced()) return false;
                }
            }
        }
        return true;
    }
}
