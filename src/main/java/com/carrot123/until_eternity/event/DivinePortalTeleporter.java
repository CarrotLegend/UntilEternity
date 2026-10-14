package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.worldgen.DivinePortalForcer;
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

public final class DivinePortalTeleporter implements ITeleporter {
    private final ServerLevel destination;
    private final BlockPos entrance;

    public DivinePortalTeleporter(ServerLevel destination, BlockPos entrance) {
        this.destination = destination;
        this.entrance = entrance.immutable();
    }

    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destinationLevel,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        if (destinationLevel != destination || !(entity.level() instanceof ServerLevel source)) return null;
        WorldBorder border = destination.getWorldBorder();
        double scale = DimensionType.getTeleportationScale(
                source.dimensionType(), destination.dimensionType());
        BlockPos target = border.clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);
        Direction.Axis axis = source.getBlockState(entrance)
                .getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);

        DivinePortalForcer forcer = new DivinePortalForcer(destination);
        Optional<BlockUtil.FoundRectangle> found = forcer.findPortalAround(target, border);
        if (found.isEmpty()) found = forcer.createPortal(target, axis);
        if (found.isEmpty()) return null;

        Vec3 relativePosition = new Vec3(0.5D, 0, 0);
        BlockState sourceState = source.getBlockState(entrance);
        if (sourceState.is(ModBlocks.DIVINE_PORTAL.get())) {
            BlockUtil.FoundRectangle sourceRectangle = BlockUtil.getLargestRectangleAround(
                    entrance, axis, 21, Direction.Axis.Y, 21,
                    p -> source.getBlockState(p).is(ModBlocks.DIVINE_PORTAL.get())
                            && source.getBlockState(p).getValue(BlockStateProperties.HORIZONTAL_AXIS) == axis);
            relativePosition = PortalShape.getRelativePosition(sourceRectangle,
                    axis, entity.position(), entity.getDimensions(entity.getPose()));
        }
        PortalInfo info = PortalShape.createPortalInfo(destination, found.get(), axis, relativePosition,
                entity, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
        if (destination.dimension().equals(DivinePortalEvents.DIVINE_REALM) && info.pos.y < 64.0D) {
            return new PortalInfo(new Vec3(info.pos.x, 64.0D, info.pos.z),
                    info.speed, info.yRot, info.xRot);
        }
        return info;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(false);
    }
}
