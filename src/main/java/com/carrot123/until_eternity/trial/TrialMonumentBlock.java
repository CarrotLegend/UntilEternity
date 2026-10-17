package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class TrialMonumentBlock extends BaseEntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty PAIRED = BooleanProperty.create("paired");

    public TrialMonumentBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.CRYING_OBSIDIAN).strength(-1, 3600000).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(PAIRED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, PAIRED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1
                || !level.getBlockState(pos.above()).canBeReplaced(context)
                || !level.getWorldBorder().isWithinBounds(pos.above())) {
            return null;
        }
        return defaultBlockState().setValue(PAIRED, true);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof ServerPlayer player
                && level.getBlockEntity(pos) instanceof TrialMonumentBlockEntity monument) {
            monument.place(player);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        Direction partnerDirection = half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        if (direction == partnerDirection) {
            if (half == DoubleBlockHalf.LOWER && !state.getValue(PAIRED)) {
                if (neighborState.is(this) && neighborState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                    return state.setValue(PAIRED, true);
                }
                return state;
            }
            if (!neighborState.is(this) || neighborState.getValue(HALF) == half) {
                return Blocks.AIR.defaultBlockState();
            }
            return state;
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? new TrialMonumentBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.TRIAL_MONUMENT.get(), TrialMonumentBlock::tickMonument);
    }

    private static void tickMonument(Level level, BlockPos pos, BlockState state,
                                     TrialMonumentBlockEntity monument) {
        if (level instanceof ServerLevel server && !state.getValue(PAIRED)) {
            BlockPos upperPos = pos.above();
            if (upperPos.getY() < server.getMaxBuildHeight()
                    && server.getBlockState(upperPos).isAir()
                    && server.getWorldBorder().isWithinBounds(upperPos)) {
                server.setBlock(upperPos, state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(PAIRED, true), 3);
                server.setBlock(pos, state.setValue(PAIRED, true), 3);
            }
        }
        TrialMonumentBlockEntity.tick(level, pos, state, monument);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos basePos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (player instanceof ServerPlayer server
                && level.getBlockState(basePos).is(this)
                && level.getBlockState(basePos).getValue(HALF) == DoubleBlockHalf.LOWER
                && level.getBlockEntity(basePos) instanceof TrialMonumentBlockEntity monument) {
            monument.interact(server);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER
                && (!state.is(next.getBlock()) || next.getValue(HALF) != DoubleBlockHalf.LOWER)
                && !level.isClientSide
                && level.getBlockEntity(pos) instanceof TrialMonumentBlockEntity monument) {
            monument.destroy();
        }
        super.onRemove(state, level, pos, next, moving);
    }
}
