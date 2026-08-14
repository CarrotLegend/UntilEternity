package com.carrot123.until_eternity.block;

import com.carrot123.until_eternity.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class FinalKeyMoldBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape NORTH_SOUTH_SHAPE = Block.box(1.0D, 0.0D, 2.0D, 15.0D, 2.0D, 14.0D);
    private static final VoxelShape EAST_WEST_SHAPE = Block.box(2.0D, 0.0D, 1.0D, 14.0D, 2.0D, 15.0D);

    public FinalKeyMoldBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 6.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? EAST_WEST_SHAPE : NORTH_SOUTH_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!heldStack.is(ModItems.FINAL_KEY_CASTING_FLUID.get())) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }

            ItemEntity finalKey = new ItemEntity(
                    level,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.25D,
                    pos.getZ() + 0.5D,
                    new ItemStack(ModItems.FINAL_KEY.get()));
            finalKey.setDeltaMovement(0.0D, 0.2D, 0.0D);
            level.addFreshEntity(finalKey);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
