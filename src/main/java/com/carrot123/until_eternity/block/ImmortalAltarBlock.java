package com.carrot123.until_eternity.block;

import com.carrot123.until_eternity.block.entity.ImmortalAltarBlockEntity;
import com.carrot123.until_eternity.block.entity.ModBlockEntities;
import com.eeeab.eeeabsmobs.sever.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public final class ImmortalAltarBlock extends BaseEntityBlock {
    public ImmortalAltarBlock() {
        super(BlockBehaviour.Properties.of()
                .noOcclusion()
                .lightLevel(state -> 7)
                .strength(5.0F, 1200.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (hit.getDirection() != Direction.UP
                || !(level.getBlockEntity(pos) instanceof ImmortalAltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }
        ItemStack heldStack = player.getItemInHand(hand);
        if (!heldStack.isEmpty()
                && !heldStack.is(ItemInit.IMMORTAL_BONE.get())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (heldStack.isEmpty() && altar.tryTake(player)) {
            return InteractionResult.CONSUME;
        }
        if (heldStack.is(ItemInit.IMMORTAL_BONE.get())
                && (altar.tryInsert(player, heldStack) || altar.tryRestart())) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ImmortalAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(
                type,
                ModBlockEntities.IMMORTAL_ALTAR.get(),
                ImmortalAltarBlockEntity::commonTick);
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean moving) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof ImmortalAltarBlockEntity altar) {
            Containers.dropContents(level, pos, altar);
            altar.clearContent();
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
