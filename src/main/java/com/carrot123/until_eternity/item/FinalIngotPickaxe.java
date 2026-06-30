package com.carrot123.until_eternity.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@SuppressWarnings("null")
public class FinalIngotPickaxe extends PickaxeItem {
    private static final int BEDROCK_BREAK_COOLDOWN = 10;

    public FinalIngotPickaxe(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return super.useOn(context);

        if (!player.isShiftKeyDown()) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.getBlockState(pos).is(Blocks.BEDROCK)) {
            return super.useOn(context);
        }

        // 检查冷却
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        breakBedrock(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        // 检查冷却
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        
        HitResult hit = player.pick(5.0, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();

        if (!level.getBlockState(pos).is(Blocks.BEDROCK)) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        breakBedrock(level, pos, player);
        return InteractionResultHolder.success(stack);
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
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.until_eternity.final_ingot_pickaxe.desc").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }
    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }

    /**
     * 破坏基岩,播放效果，应用冷却时间
     */
    private void breakBedrock(Level level, BlockPos pos, Player player) {
        // 粒子效果
        level.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(Blocks.BEDROCK.defaultBlockState()));
        // 音效
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        // 移除基岩
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        // 半秒冷却
        player.getCooldowns().addCooldown(this, BEDROCK_BREAK_COOLDOWN);
    }
}
