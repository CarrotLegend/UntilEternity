package com.carrot123.until_eternity.compat.goetyrevelation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class BlessingScrollTooltips {
    private BlessingScrollTooltips() {
    }

    public static void append(ItemStack stack, List<Component> tooltip) {
        int tier = BlessingScrollDamageCap.getTier(stack);
        tooltip.add(Component.empty());
        if (tier == BlessingScrollDamageCap.UNLIMITED_TIER) {
            tooltip.add(Component.translatable(
                    "tooltip.until_eternity.blessing_scroll.damage_cap_unlimited"
            ).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable(
                    "tooltip.until_eternity.blessing_scroll.fully_unlocked"
            ).withStyle(ChatFormatting.DARK_PURPLE));
            return;
        }
        tooltip.add(Component.translatable(
                "tooltip.until_eternity.blessing_scroll.damage_cap",
                capText(tier)
        ).withStyle(ChatFormatting.GOLD));
        int nextTier = tier + 1;
        ItemStack material = BlessingScrollDamageCap.getMaterialStackForTier(nextTier);
        ResourceLocation materialId = BlessingScrollDamageCap.getMaterialForTier(nextTier);
        Component materialName = material.isEmpty()
                ? Component.literal(String.valueOf(materialId))
                : material.getHoverName();
        if (nextTier == BlessingScrollDamageCap.UNLIMITED_TIER) {
            tooltip.add(Component.translatable(
                    "tooltip.until_eternity.blessing_scroll.next_unlimited",
                    materialName
            ).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable(
                    "tooltip.until_eternity.blessing_scroll.next_cap",
                    materialName, capText(nextTier)
            ).withStyle(ChatFormatting.GRAY));
        }
    }

    private static String capText(int tier) {
        return switch (tier) {
            case BlessingScrollDamageCap.DRAGONSKIN_TIER -> "400%";
            case BlessingScrollDamageCap.BLUEPRINT_TIER -> "600%";
            default -> "200%";
        };
    }
}
