package com.carrot123.until_eternity.worldgen;

import com.carrot123.until_eternity.block.ModBlocks;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class PortalShape {
    private static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;
    private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0F;
    private static final double SAFE_TRAVEL_MAX_VERTICAL_DELTA = 1.0;
    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;
    @Nullable
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    /** Frame predicate: only deepslate variants are accepted as frame material. */
    private static boolean isFrame(BlockState state) {
        return state.getBlock().getDescriptionId().contains("deepslate");
    }

    public static Optional<PortalShape> findEmptyPortalShape(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        return findPortalShape(level, pos, shape -> shape.isValid() && shape.numPortalBlocks == 0, axis);
    }

    public static Optional<PortalShape> findPortalShape(LevelAccessor level, BlockPos pos,
                                                         Predicate<PortalShape> filter, Direction.Axis axis) {
        Optional<PortalShape> opt = Optional.of(new PortalShape(level, pos, axis)).filter(filter);
        if (opt.isPresent()) {
            return opt;
        } else {
            Direction.Axis other = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            return Optional.of(new PortalShape(level, pos, other)).filter(filter);
        }
    }

    public PortalShape(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        this.level = level;
        this.axis = axis;
        this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = this.calculateBottomLeft(pos);
        if (this.bottomLeft == null) {
            this.bottomLeft = pos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.height = this.calculateHeight();
            }
        }
    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos pos) {
        int minY = Math.max(this.level.getMinBuildHeight(), pos.getY() - 21);
        while (pos.getY() > minY && isEmpty(this.level.getBlockState(pos.below()))) {
            pos = pos.below();
        }
        Direction opposite = this.rightDir.getOpposite();
        int dist = this.getDistanceUntilEdgeAboveFrame(pos, opposite) - 1;
        return dist < 0 ? null : pos.relative(opposite, dist);
    }

    private int calculateWidth() {
        int w = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
        return w >= 2 && w <= 21 ? w : 0;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos pos, Direction dir) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= 21; i++) {
            mut.set(pos).move(dir, i);
            BlockState state = this.level.getBlockState(mut);
            if (!isEmpty(state)) {
                if (isFrame(state)) {
                    return i;
                }
                break;
            }
            BlockState below = this.level.getBlockState(mut.move(Direction.DOWN));
            if (!isFrame(below)) {
                break;
            }
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int h = this.getDistanceUntilTop(mut);
        return h >= 3 && h <= 21 && this.hasTopFrame(mut, h) ? h : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos mut, int h) {
        for (int i = 0; i < this.width; i++) {
            BlockPos.MutableBlockPos m = mut.set(this.bottomLeft)
                    .move(Direction.UP, h).move(this.rightDir, i);
            if (!isFrame(this.level.getBlockState(m))) {
                return false;
            }
        }
        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos mut) {
        for (int y = 0; y < 21; y++) {
            mut.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir, -1);
            if (!isFrame(this.level.getBlockState(mut))) {
                return y;
            }
            mut.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir, this.width);
            if (!isFrame(this.level.getBlockState(mut))) {
                return y;
            }
            for (int x = 0; x < this.width; x++) {
                mut.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir, x);
                BlockState s = this.level.getBlockState(mut);
                if (!isEmpty(s)) {
                    return y;
                }
                if (s.is(ModBlocks.CHAOS_PORTAL.get())) {
                    this.numPortalBlocks++;
                }
            }
        }
        return 21;
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.is(ModBlocks.CHAOS_PORTAL.get());
    }

    public boolean isValid() {
        return this.bottomLeft != null
                && this.width >= 2 && this.width <= 21
                && this.height >= 3 && this.height <= 21;
    }

    public void createPortalBlocks() {
        BlockState portalState = ModBlocks.CHAOS_PORTAL.get().defaultBlockState()
                .setValue(com.carrot123.until_eternity.block.ChaosPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft,
                this.bottomLeft.relative(Direction.UP, this.height - 1)
                        .relative(this.rightDir, this.width - 1))
                .forEach(p -> this.level.setBlock(p, portalState, 18));
    }

    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }

    public static Vec3 getRelativePosition(BlockUtil.FoundRectangle rect, Direction.Axis axis,
                                            Vec3 pos, EntityDimensions dims) {
        double d0 = (double)rect.axis1Size - dims.width;
        double d1 = (double)rect.axis2Size - dims.height;
        BlockPos minCorner = rect.minCorner;
        double relX;
        if (d0 > 0.0) {
            float f = minCorner.get(axis) + dims.width / 2.0F;
            relX = Mth.clamp(Mth.inverseLerp(pos.get(axis) - f, 0.0, d0), 0.0, 1.0);
        } else {
            relX = 0.5;
        }
        double relY;
        if (d1 > 0.0) {
            Direction.Axis yAxis = Direction.Axis.Y;
            relY = Mth.clamp(Mth.inverseLerp(pos.get(yAxis) - minCorner.get(yAxis), 0.0, d1), 0.0, 1.0);
        } else {
            relY = 0.0;
        }
        Direction.Axis otherAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        double relZ = pos.get(otherAxis) - (minCorner.get(otherAxis) + 0.5);
        return new Vec3(relX, relY, relZ);
    }

    public static net.minecraft.world.level.portal.PortalInfo createPortalInfo(
            ServerLevel destLevel, BlockUtil.FoundRectangle destRect,
            Direction.Axis sourceAxis, Vec3 relativePos, Entity entity,
            Vec3 velocity, float yRot, float xRot) {
        BlockPos minCorner = destRect.minCorner;
        BlockState portalState = destLevel.getBlockState(minCorner);
        Direction.Axis destAxis = portalState.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS)
                .orElse(Direction.Axis.X);
        double axis1Size = destRect.axis1Size;
        double axis2Size = destRect.axis2Size;
        EntityDimensions dims = entity.getDimensions(entity.getPose());
        int angleAdjust = sourceAxis == destAxis ? 0 : 90;
        Vec3 adjustedVel = sourceAxis == destAxis ? velocity : new Vec3(velocity.z, velocity.y, -velocity.x);
        double entX = dims.width / 2.0 + (axis1Size - dims.width) * relativePos.x();
        double entY = (axis2Size - dims.height) * relativePos.y();
        double entZ = 0.5 + relativePos.z();
        boolean isX = destAxis == Direction.Axis.X;
        Vec3 pos = new Vec3(
                minCorner.getX() + (isX ? entX : entZ),
                minCorner.getY() + entY,
                minCorner.getZ() + (isX ? entZ : entX));
        Vec3 safePos = findCollisionFreePosition(pos, destLevel, entity, dims);
        return new net.minecraft.world.level.portal.PortalInfo(safePos, adjustedVel, yRot + angleAdjust, xRot);
    }

    private static Vec3 findCollisionFreePosition(Vec3 pos, ServerLevel level, Entity entity, EntityDimensions dims) {
        if (dims.width > 4.0F || dims.height > 4.0F) {
            return pos;
        }
        double halfHeight = dims.height / 2.0;
        Vec3 center = pos.add(0.0, halfHeight, 0.0);
        VoxelShape shape = Shapes.create(
                AABB.ofSize(center, dims.width, 0.0, dims.width)
                        .expandTowards(0.0, 1.0, 0.0).inflate(1.0E-6));
        Optional<Vec3> found = level.findFreePosition(entity, shape, center, dims.width, dims.height, dims.width);
        Optional<Vec3> result = found.map(v -> v.subtract(0.0, halfHeight, 0.0));
        return result.orElse(pos);
    }
}
