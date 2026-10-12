package com.carrot123.until_eternity.worldgen;

import com.carrot123.until_eternity.block.DivinePortalBlock;
import com.carrot123.until_eternity.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public final class DivinePortalShape {
    private static final int MAX_WIDTH = 21;
    private static final int MAX_HEIGHT = 21;
    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction rightDir;
    @Nullable private BlockPos bottomLeft;
    private final int width;
    private int height;
    private int portalBlocks;

    public DivinePortalShape(LevelAccessor level, BlockPos inside, Direction.Axis axis) {
        this.level = level;
        this.axis = axis;
        this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = calculateBottomLeft(inside);
        if (bottomLeft == null) {
            width = 0;
        } else {
            width = calculateWidth();
            if (width > 0) height = calculateHeight();
        }
    }

    public static Optional<DivinePortalShape> findEmpty(LevelAccessor level, BlockPos inside) {
        DivinePortalShape x = new DivinePortalShape(level, inside, Direction.Axis.X);
        if (x.isValid() && x.portalBlocks == 0) return Optional.of(x);
        DivinePortalShape z = new DivinePortalShape(level, inside, Direction.Axis.Z);
        return z.isValid() && z.portalBlocks == 0 ? Optional.of(z) : Optional.empty();
    }

    private boolean isFrame(BlockState state) {
        return state.is(ModBlocks.DIVINE_CALCITE.get());
    }

    private boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.is(ModBlocks.DIVINE_PORTAL.get());
    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos pos) {
        int limit = Math.max(level.getMinBuildHeight(), pos.getY() - MAX_HEIGHT);
        while (pos.getY() > limit && isEmpty(level.getBlockState(pos.below()))) {
            pos = pos.below();
        }
        int distance = distanceUntilEdge(pos, rightDir.getOpposite()) - 1;
        return distance < 0 ? null : pos.relative(rightDir.getOpposite(), distance);
    }

    private int calculateWidth() {
        int distance = distanceUntilEdge(bottomLeft, rightDir);
        return distance >= 2 && distance <= MAX_WIDTH ? distance : 0;
    }

    private int distanceUntilEdge(BlockPos pos, Direction direction) {
        BlockPos.MutableBlockPos test = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= MAX_WIDTH; i++) {
            test.set(pos).move(direction, i);
            if (!isEmpty(level.getBlockState(test))) {
                return isFrame(level.getBlockState(test)) ? i : 0;
            }
            if (!isFrame(level.getBlockState(test.below()))) return 0;
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos test = new BlockPos.MutableBlockPos();
        for (int y = 0; y <= MAX_HEIGHT; y++) {
            if (!isFrame(level.getBlockState(test.set(bottomLeft).move(rightDir, -1).move(Direction.UP, y)))
                    || !isFrame(level.getBlockState(test.set(bottomLeft).move(rightDir, width).move(Direction.UP, y)))) {
                return 0;
            }
            for (int x = 0; x < width; x++) {
                test.set(bottomLeft).move(rightDir, x).move(Direction.UP, y);
                BlockState current = level.getBlockState(test);
                if (!isEmpty(current)) {
                    if (y < 3 || y > MAX_HEIGHT) return 0;
                    for (int column = 0; column < width; column++) {
                        if (!isFrame(level.getBlockState(test.set(bottomLeft)
                                .move(rightDir, column).move(Direction.UP, y)))) return 0;
                    }
                    return y;
                }
                if (current.is(ModBlocks.DIVINE_PORTAL.get())) {
                    if (current.getValue(DivinePortalBlock.AXIS) != axis) return 0;
                    portalBlocks++;
                }
            }
        }
        return 0;
    }

    public boolean isValid() {
        return bottomLeft != null && width >= 2 && width <= MAX_WIDTH
                && height >= 3 && height <= MAX_HEIGHT;
    }

    public boolean isComplete() {
        return isValid() && portalBlocks == width * height;
    }

    public void createPortalBlocks() {
        if (!isValid()) return;
        BlockState state = ModBlocks.DIVINE_PORTAL.get().defaultBlockState()
                .setValue(DivinePortalBlock.AXIS, axis);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                level.setBlock(bottomLeft.relative(rightDir, x).above(y), state, 18);
            }
        }
    }
}
