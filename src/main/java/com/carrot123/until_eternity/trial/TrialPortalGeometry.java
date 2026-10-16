package com.carrot123.until_eternity.trial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The visual disk and the server trigger share exactly the same dimensions. */
public final class TrialPortalGeometry {
    public static final int WIDTH = 4;
    public static final int HEIGHT = 5;
    public static final double RADIUS = 2.0;
    public static final double CENTER_Y = 2.25;
    private static final double PLANE_HALF_THICKNESS = 0.2;

    private TrialPortalGeometry() {}

    public static Direction along(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    public static BlockPos cell(BlockPos anchor, Direction.Axis axis, int horizontal, int vertical) {
        return anchor.relative(along(axis), horizontal).above(vertical);
    }

    public static Vec3 center(BlockPos anchor, Direction.Axis axis) {
        return axis == Direction.Axis.X
                ? new Vec3(anchor.getX() + 2, anchor.getY() + CENTER_Y, anchor.getZ() + 0.5)
                : new Vec3(anchor.getX() + 0.5, anchor.getY() + CENTER_Y, anchor.getZ() + 2);
    }

    public static boolean intersects(BlockPos anchor, Direction.Axis axis, AABB box) {
        Vec3 center = center(anchor, axis);
        double plane = axis == Direction.Axis.X ? center.z : center.x;
        double normalMin = axis == Direction.Axis.X ? box.minZ : box.minX;
        double normalMax = axis == Direction.Axis.X ? box.maxZ : box.maxX;
        if (normalMin > plane + PLANE_HALF_THICKNESS || normalMax < plane - PLANE_HALF_THICKNESS) return false;
        double alongMin = axis == Direction.Axis.X ? box.minX : box.minZ;
        double alongMax = axis == Direction.Axis.X ? box.maxX : box.maxZ;
        double alongCenter = axis == Direction.Axis.X ? center.x : center.z;
        return intersectsDisk(alongMin - alongCenter, alongMax - alongCenter,
                box.minY - center.y, box.maxY - center.y);
    }

    public static boolean intersectsDisk(double minX, double maxX, double minY, double maxY) {
        double dx = Math.max(minX, Math.max(0, -maxX));
        double dy = Math.max(minY, Math.max(0, -maxY));
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }
}
