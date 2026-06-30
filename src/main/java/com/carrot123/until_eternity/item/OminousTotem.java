package com.carrot123.until_eternity.item;

import javax.annotation.Nonnull;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class OminousTotem extends Item {
    public OminousTotem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack stack) {
        return 20; // 一秒
    }

    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        return UseAnim.BOW; // 移速降低视角拉大
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity entity) {
        if (!level.isClientSide) {
            // 给予一个小时不祥之兆
            entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 72000, 4));
        }
        // 破碎音效
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        // 消耗图腾
        stack.shrink(1);
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }
}
