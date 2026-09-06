package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.event.BloodSplashDaggerEvents;
import com.carrot123.until_eternity.registry.ModDamageTypes;
import com.carrot123.until_eternity.registry.ModPotions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class BloodSplashDaggerItem extends SwordItem {
    public static final float SELF_DAMAGE = 4.0F;
    public static final float SACRIFICE_DAMAGE = Float.MAX_VALUE;

    public BloodSplashDaggerItem(Properties properties) {
        super(Tiers.STONE, 3, -2.4F, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand) {
        ItemStack dagger = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(dagger);
        }

        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                return InteractionResultHolder.sidedSuccess(dagger, true);
            }
            damageDagger(dagger, player);
            player.hurt(ModDamageTypes.sacrifice(level), SACRIFICE_DAMAGE);
            return InteractionResultHolder.sidedSuccess(dagger, false);
        }

        if (!player.getOffhandItem().is(Items.GLASS_BOTTLE)) {
            return InteractionResultHolder.pass(dagger);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(dagger, true);
        }

        float healthBefore = player.getHealth();
        boolean accepted = player.hurt(
                player.damageSources().playerAttack(player),
                SELF_DAMAGE);
        if (!accepted || !(player.getHealth() < healthBefore)) {
            return InteractionResultHolder.fail(dagger);
        }

        damageDagger(dagger, player);
        BloodBottleHelper.consumeOffhandBottleAndGive(
                player,
                ModPotions.TRUE_BLOOD.get());
        return InteractionResultHolder.sidedSuccess(dagger, false);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(
                    "tooltip.until_eternity.blood_dagger.hint"
            ).withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable(
                "tooltip.until_eternity.blood_dagger.line1"
        ).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "tooltip.until_eternity.blood_dagger.line2"
        ).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(
                "tooltip.until_eternity.blood_dagger.line3"
        ).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(
                "tooltip.until_eternity.blood_dagger.line4"
        ).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(
                "tooltip.until_eternity.blood_dagger.line5"
        ).withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public boolean hurtEnemy(
            ItemStack stack,
            LivingEntity target,
            LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            BloodSplashDaggerEvents.completeSuccessfulHit(player, target);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private static void damageDagger(ItemStack dagger, Player player) {
        dagger.hurtAndBreak(1, player, brokenPlayer ->
                brokenPlayer.broadcastBreakEvent(EquipmentSlot.MAINHAND));
    }
}
