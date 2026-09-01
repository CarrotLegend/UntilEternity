package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.worldgen.PortalForcer;
import com.carrot123.until_eternity.worldgen.PortalShape;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public final class ChaosPortalTeleporter implements ITeleporter {
    private final ServerLevel destination;
    private final BlockPos entrancePos;

    public ChaosPortalTeleporter(ServerLevel destination, BlockPos entrancePos) {
        this.destination = destination;
        this.entrancePos = entrancePos.immutable();
    }

    @Override
    @Nullable
    public PortalInfo getPortalInfo(
            Entity entity,
            ServerLevel destinationLevel,
            Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        if (destinationLevel != destination
                || !(entity.level() instanceof ServerLevel sourceLevel)) {
            return null;
        }

        WorldBorder border = destinationLevel.getWorldBorder();
        double scale = DimensionType.getTeleportationScale(
                sourceLevel.dimensionType(), destinationLevel.dimensionType());
        BlockPos targetPos = border.clampToBounds(
                entity.getX() * scale, entity.getY(), entity.getZ() * scale);

        Direction.Axis sourceAxis = getPortalAxis(sourceLevel, entrancePos);
        PortalForcer portalForcer = new PortalForcer(destinationLevel);
        Optional<BlockUtil.FoundRectangle> destinationPortal =
                portalForcer.findPortalAround(targetPos, border);
        if (destinationPortal.isEmpty()) {
            destinationPortal = portalForcer.createPortal(targetPos, sourceAxis);
        }
        if (destinationPortal.isEmpty()) {
            return null;
        }

        BlockState sourceState = sourceLevel.getBlockState(entrancePos);
        Vec3 relativePosition = new Vec3(0.5D, 0.0D, 0.0D);
        if (sourceState.is(ModBlocks.CHAOS_PORTAL.get())
                && sourceState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            BlockUtil.FoundRectangle sourcePortal = BlockUtil.getLargestRectangleAround(
                    entrancePos,
                    sourceAxis,
                    21,
                    Direction.Axis.Y,
                    21,
                    pos -> isPortalWithAxis(sourceLevel.getBlockState(pos), sourceAxis));
            relativePosition = PortalShape.getRelativePosition(
                    sourcePortal,
                    sourceAxis,
                    entity.position(),
                    entity.getDimensions(entity.getPose()));
        }

        return PortalShape.createPortalInfo(
                destinationLevel,
                destinationPortal.get(),
                sourceAxis,
                relativePosition,
                entity,
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot());
    }

    @Override
    public Entity placeEntity(
            Entity entity,
            ServerLevel currentWorld,
            ServerLevel destinationWorld,
            float yaw,
            Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(false);
    }

    private static Direction.Axis getPortalAxis(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos)
                .getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS)
                .orElse(Direction.Axis.X);
    }

    private static boolean isPortalWithAxis(BlockState state, Direction.Axis axis) {
        return state.is(ModBlocks.CHAOS_PORTAL.get())
                && state.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS)
                .map(value -> value == axis)
                .orElse(false);
    }
}
