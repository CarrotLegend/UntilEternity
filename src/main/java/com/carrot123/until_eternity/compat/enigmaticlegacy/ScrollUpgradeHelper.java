package com.carrot123.until_eternity.compat.enigmaticlegacy;

import com.carrot123.until_eternity.compat.goetyrevelation.BlessingScrollDamageCap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ScrollUpgradeHelper {
    private ScrollUpgradeHelper() {
    }

    public static boolean tryUpgrade(Player player, InteractionHand usedHand) {
        InteractionHand otherHand = usedHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack used = player.getItemInHand(usedHand);
        ItemStack other = player.getItemInHand(otherHand);

        if (matches(used, other)) {
            return upgrade(player, used, other);
        }
        if (matches(other, used)) {
            return upgrade(player, other, used);
        }
        return false;
    }

    private static boolean matches(ItemStack scroll, ItemStack material) {
        return (CursedScrollDamageCap.isCursedScroll(scroll)
                && CursedScrollDamageCap.getUpgradeTier(material) >= 0)
                || (BlessingScrollDamageCap.isBlessingScroll(scroll)
                && BlessingScrollDamageCap.getUpgradeTier(material) >= 0);
    }

    private static boolean upgrade(Player player, ItemStack scroll, ItemStack material) {
        boolean cursed = CursedScrollDamageCap.isCursedScroll(scroll);
        int targetTier = cursed
                ? CursedScrollDamageCap.getUpgradeTier(material)
                : BlessingScrollDamageCap.getUpgradeTier(material);
        int currentTier = cursed
                ? CursedScrollDamageCap.getTier(scroll)
                : BlessingScrollDamageCap.getTier(scroll);
        int unlimitedTier = cursed
                ? CursedScrollDamageCap.UNLIMITED_TIER
                : BlessingScrollDamageCap.UNLIMITED_TIER;
        if (player.level().isClientSide) {
            return true;
        }
        String prefix = cursed ? "cursed_scroll" : "blessing_scroll";
        if (cursed ? targetTier <= currentTier
                : !BlessingScrollDamageCap.canUpgrade(currentTier, targetTier)) {
            player.displayClientMessage(Component.translatable(
                    "message.until_eternity." + prefix + ".cap_already_unlocked"
            ).withStyle(ChatFormatting.RED), true);
            return true;
        }
        if (cursed) {
            CursedScrollDamageCap.setTier(scroll, targetTier);
        } else {
            BlessingScrollDamageCap.setTier(scroll, targetTier);
        }
        if (!player.getAbilities().instabuild) {
            material.shrink(1);
        }
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                1.0F, targetTier == unlimitedTier ? 1.25F : 1.0F + targetTier * 0.05F);
        String key = "message.until_eternity." + prefix + ".upgraded";
        Component message = targetTier == unlimitedTier
                ? Component.translatable(key + "_unlimited")
                : Component.translatable(key, capText(cursed, targetTier));
        player.displayClientMessage(message.copy().withStyle(ChatFormatting.GOLD), true);
        return true;
    }

    private static String capText(boolean cursed, int tier) {
        if (!cursed) {
            return switch (tier) {
                case BlessingScrollDamageCap.DRAGONSKIN_TIER -> "400%";
                case BlessingScrollDamageCap.BLUEPRINT_TIER -> "600%";
                default -> "200%";
            };
        }
        return switch (tier) {
            case CursedScrollDamageCap.ABYSSAL_TIER -> "400%";
            case CursedScrollDamageCap.DIVINE_TIER -> "600%";
            case CursedScrollDamageCap.MONSTROUS_TIER -> "800%";
            default -> "200%";
        };
    }
}
