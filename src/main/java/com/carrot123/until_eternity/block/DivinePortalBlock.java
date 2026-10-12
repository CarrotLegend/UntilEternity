package com.carrot123.until_eternity.block;

import com.carrot123.until_eternity.client.portal.PortalVisualTracker;
import com.carrot123.until_eternity.client.portal.PortalVisualType;
import com.carrot123.until_eternity.event.DivinePortalEvents;
import com.carrot123.until_eternity.particle.ModParticles;
import com.carrot123.until_eternity.worldgen.DivinePortalShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class DivinePortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape X_SHAPE = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape Z_SHAPE = Block.box(6, 0, 0, 10, 16, 16);

    public DivinePortalBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)
                .noCollission()
                .strength(-1.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 13)
                .pushReaction(PushReaction.BLOCK)
                .noLootTable()
                .noOcclusion()
                .isValidSpawn((state, reader, pos, type) -> false));
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis axis = state.getValue(AXIS);
        boolean otherHorizontalAxis = direction.getAxis().isHorizontal() && direction.getAxis() != axis;
        if (!otherHorizontalAxis && !neighbor.is(this)
                && !new DivinePortalShape(level, pos, axis).isComplete()) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.canChangeDimensions()) return;
        if (level.isClientSide) {
            PortalVisualTracker.markIfLocalPlayer(entity, PortalVisualType.DIVINE);
            entity.handleInsidePortal(pos);
        } else {
            DivinePortalEvents.entityInside(level, pos, entity);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.35F,
                    1.1F + random.nextFloat() * 0.25F, false);
        }
        for (int i = 0; i < 2; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double dx = (random.nextDouble() - 0.5D) * 0.3D;
            double dy = (random.nextDouble() - 0.5D) * 0.3D;
            double dz = (random.nextDouble() - 0.5D) * 0.3D;
            int sign = random.nextBoolean() ? 1 : -1;
            if (state.getValue(AXIS) == Direction.Axis.Z) {
                x = pos.getX() + 0.5D + sign * 0.25D;
                dx = (0.3D + random.nextDouble() * 0.7D) * sign;
            } else {
                z = pos.getZ() + 0.5D + sign * 0.25D;
                dz = (0.3D + random.nextDouble() * 0.7D) * sign;
            }
            level.addParticle(ModParticles.DIVINE_PORTAL_PARTICLE.get(), x, y, z, dx, dy, dz);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            return state.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.X
                    ? Direction.Axis.Z : Direction.Axis.X);
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
}
