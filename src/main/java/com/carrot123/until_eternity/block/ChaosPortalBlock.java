package com.carrot123.until_eternity.block;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

@SuppressWarnings("null")
public class ChaosPortalBlock extends Block {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public static final ResourceKey<Level> CHAOS_REALM =
            ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                    new ResourceLocation(until_eternity.MODID + ":chaos_realm"));

    public ChaosPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    @Override
    public boolean isPossibleToRespawnInThis(@Nonnull BlockState state) {
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        Direction.Axis axis = context.getHorizontalDirection().getAxis();
        return this.defaultBlockState().setValue(AXIS, axis);
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public void entityInside(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        if (entity.canChangeDimensions() && !entity.isPassenger() && !entity.isVehicle()) {
            handleTeleport(level, pos, entity);
        }
    }

    private void handleTeleport(Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide) {
            ResourceKey<Level> targetDim;
            ResourceKey<Level> sourceDim = level.dimension();

            if (sourceDim == CHAOS_REALM) {
                targetDim = Level.OVERWORLD;
            } else {
                targetDim = CHAOS_REALM;
            }

            ServerLevel serverLevel = level.getServer().getLevel(targetDim);
            if (serverLevel == null) return;

            // Creative mode — instant teleport
            if (entity instanceof ServerPlayer player && player.isCreative()) {
                // Play portal sound
                level.playSound(null, pos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.5F, 1.0F);

                // Find safe destination
                BlockPos destPos = findSafePos(serverLevel, entity.blockPosition());
                player.teleportTo(serverLevel, destPos.getX() + 0.5, destPos.getY(),
                        destPos.getZ() + 0.5, entity.getYRot(), entity.getXRot());
                return;
            }

            // Survival mode — 2-second delay with cooldown
            if (entity instanceof Player) {
                entity.getPersistentData().putLong("chaos_portal_cooldown",
                        level.getGameTime() + 40); // 2-second cooldown

                // Schedule teleport after 2 seconds (40 ticks)
                level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.5F, 1.0F);
            }

            // Check cooldown
            if (entity.getPersistentData().contains("chaos_portal_cooldown")) {
                long cooldown = entity.getPersistentData().getLong("chaos_portal_cooldown");
                if (level.getGameTime() >= cooldown) {
                    entity.getPersistentData().remove("chaos_portal_cooldown");
                    doTeleport(entity, serverLevel);
                }
                return;
            }

            // First entry — start the delay
            entity.getPersistentData().putLong("chaos_portal_cooldown",
                    level.getGameTime() + 40);
        }
    }

    private void doTeleport(Entity entity, ServerLevel targetLevel) {
        BlockPos destPos = findSafePos(targetLevel, entity.blockPosition());

        if (entity instanceof ServerPlayer player) {
            player.teleportTo(targetLevel, destPos.getX() + 0.5, destPos.getY(),
                    destPos.getZ() + 0.5, player.getYRot(), player.getXRot());
        } else {
            // For non-player entities
            entity.changeDimension(targetLevel, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerLevel currentWorld,
                                          ServerLevel destWorld, float yaw,
                                          Function<Boolean, Entity> repositionEntity) {
                    entity.moveTo(destPos.getX() + 0.5, destPos.getY(),
                            destPos.getZ() + 0.5, yaw, entity.getXRot());
                    return entity;
                }
            });
        }
    }

    private BlockPos findSafePos(ServerLevel level, BlockPos from) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(from.getX(), 65, from.getZ());
        // Find the surface
        while (pos.getY() > level.getMinBuildHeight() && level.getBlockState(pos).isAir()) {
            pos.move(Direction.DOWN);
        }
        while (pos.getY() < level.getMaxBuildHeight() - 1 &&
                !level.getBlockState(pos.above()).isAir()) {
            pos.move(Direction.UP);
        }
        // Ensure the destination is safe
        for (int y = 0; y < 3; y++) {
            BlockPos checkPos = pos.above(y);
            if (!level.getBlockState(checkPos).isAir()
                    && !level.getBlockState(checkPos).canBeReplaced()) {
                pos.move(Direction.UP, 3);
                break;
            }
        }
        return pos.above();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0 && level.isEmptyBlock(pos.above())) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS,
                    0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        for (int i = 0; i < 4; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double mx = (random.nextDouble() - 0.5) * 0.5;
            double my = (random.nextDouble() - 0.5) * 0.5;
            double mz = (random.nextDouble() - 0.5) * 0.5;

            level.addParticle(ParticleTypes.PORTAL, x, y, z, mx, my, mz);
        }
    }
}
