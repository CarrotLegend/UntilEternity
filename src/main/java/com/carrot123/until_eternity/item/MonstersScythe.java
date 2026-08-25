package com.carrot123.until_eternity.item;

import com.eeeab.eeeabsmobs.sever.init.EffectInit;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

@SuppressWarnings("null")
public class MonstersScythe extends SwordItem {

    private static final int FRENZY_DURATION = 20 * 10;
    private static final int FRENZY_AMPLIFIER = 1;
    private static final int FRENZY_COOLDOWN = 20 * 30;

    public MonstersScythe(
            Tier tier,
            int attackDamageModifier,
            float attackSpeedModifier,
            Properties props
    ) {
        super(
                tier,
                attackDamageModifier,
                attackSpeedModifier,
                props
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            @Nonnull Level level,
            @Nonnull Player player,
            @Nonnull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            player.addEffect(
                    new MobEffectInstance(
                            EffectInit.FRENZY_EFFECT.get(),
                            FRENZY_DURATION,
                            FRENZY_AMPLIFIER
                    )
            );
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.BELL_BLOCK,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
            player.getCooldowns().addCooldown(
                    this,
                    FRENZY_COOLDOWN
            );
            player.swing(
                    InteractionHand.MAIN_HAND,
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide
        );
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