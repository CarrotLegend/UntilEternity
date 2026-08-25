package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.compat.SummoningRitualsCompat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@SuppressWarnings("null")
public class FinalIngotPickaxe extends PickaxeItem {
    private static final int UNBREAKABLE_BREAK_COOLDOWN = 10;

    public FinalIngotPickaxe(
            Tier tier,
            int attackDamageModifier,
            float attackSpeedModifier,
            Properties properties
    ) {
        super(
                tier,
                attackDamageModifier,
                attackSpeedModifier,
                properties
        );
    }

    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Player player = context.getPlayer();

        if (player == null) {
            return super.useOn(context);
        }

        if (!player.isShiftKeyDown()) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!isUnbreakable(level, pos)) {
            return super.useOn(context);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean destroyed = tryBreakUnbreakableBlock(
                level,
                pos,
                player
        );

        return destroyed
                ? InteractionResult.SUCCESS
                : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            @Nonnull Level level,
            @Nonnull Player player,
            @Nonnull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        HitResult hit = player.pick(
                5.0D,
                0.0F,
                false
        );

        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();

        if (!isUnbreakable(level, pos)) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        boolean destroyed = tryBreakUnbreakableBlock(
                level,
                pos,
                player
        );

        return destroyed
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.fail(stack);
    }

    private static boolean isUnbreakable(
            Level level,
            BlockPos pos
    ) {
        BlockState state = level.getBlockState(pos);

        if (state.isAir()) {
            return false;
        }

        return state.getDestroySpeed(level, pos) < 0.0F;
    }

    public boolean tryBreakUnbreakableBlock(
            Level level,
            BlockPos pos,
            Player player
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        }

        BlockState originalState = serverLevel.getBlockState(pos);

        if (originalState.isAir()) {
            return false;
        }

        if (originalState.getDestroySpeed(serverLevel, pos) >= 0.0F) {
            return false;
        }

        boolean isIndestructibleAltar =
                SummoningRitualsCompat.isIndestructibleAltar(
                        originalState
                );

        Set<UUID> previousItemEntities;

        if (isIndestructibleAltar) {
            previousItemEntities =
                    SummoningRitualsCompat.snapshotNearbyItemEntities(
                            serverLevel,
                            pos
                    );
        } else {
            previousItemEntities = Set.of();
        }

        boolean destroyed =
                serverPlayer.gameMode.destroyBlock(pos);

        if (!destroyed) {
            return false;
        }

        serverLevel.levelEvent(
                2001,
                pos,
                Block.getId(originalState)
        );

        if (isIndestructibleAltar) {
            SummoningRitualsCompat
                    .replaceNormalAltarDropWithIndestructibleAltar(
                            serverLevel,
                            pos,
                            previousItemEntities
                    );
        }

        player.getCooldowns().addCooldown(
                this,
                UNBREAKABLE_BREAK_COOLDOWN
        );

        return true;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isBarVisible(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nullable Level level,
            @Nonnull List<Component> tooltip,
            @Nonnull TooltipFlag flag
    ) {
        tooltip.add(
                Component.translatable("item.unbreakable")
                        .withStyle(ChatFormatting.BLUE)
        );

        tooltip.add(
                Component.translatable(
                                "item.until_eternity.final_ingot_pickaxe.desc"
                        )
                        .withStyle(ChatFormatting.GOLD)
        );

        super.appendHoverText(
                stack,
                level,
                tooltip,
                flag
        );
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }
}
