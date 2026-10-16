
package com.carrot123.until_eternity.compat.goetyrevelation;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class VenomousWhipUpgradeHelper {

    public static final String LEVEL_TAG =
            "until_eternity:venomous_whip_level";

    public static final int MAX_LEVEL = 5;

    private static final ResourceLocation WHIP_ID =
            new ResourceLocation(
                    "goety_revelation",
                    "venomous_spider_whip"
            );

    private static final int[] BONUSES = {
            25, 50, 100, 150, 300, 600
    };

    private VenomousWhipUpgradeHelper() {
    }

    public static boolean isWhip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        return WHIP_ID.equals(
                ForgeRegistries.ITEMS.getKey(stack.getItem())
        );
    }

    public static int getLevel(ItemStack stack) {
        if (!isWhip(stack) || stack.getTag() == null) {
            return 0;
        }

        if (!stack.getTag().contains(LEVEL_TAG, Tag.TAG_INT)) {
            return 0;
        }

        int level = stack.getTag().getInt(LEVEL_TAG);

        return level >= 0 && level <= MAX_LEVEL
                ? level
                : 0;
    }

    public static int getNextLevel(ItemStack stack) {
        if (!isWhip(stack)) {
            return 0;
        }

        if (stack.getTag() != null
                && stack.getTag().contains(LEVEL_TAG)) {
            if (!stack.getTag().contains(LEVEL_TAG, Tag.TAG_INT)) {
                return 0;
            }

            int stored = stack.getTag().getInt(LEVEL_TAG);

            if (stored < 0 || stored >= MAX_LEVEL) {
                return 0;
            }
        }

        return getLevel(stack) + 1;
    }

    public static void setLevel(ItemStack stack, int level) {
        if (!isWhip(stack) || level < 0 || level > MAX_LEVEL) {
            return;
        }

        if (level == 0) {
            if (stack.getTag() != null) {
                stack.getTag().remove(LEVEL_TAG);
            }
            return;
        }

        stack.getOrCreateTag().putInt(LEVEL_TAG, level);
    }

    public static double getBonus(ItemStack stack) {
        return BONUSES[getLevel(stack)] / 100.0D;
    }

    public static ResourceLocation getRequiredMaterial(int targetLevel) {
        return switch (targetLevel) {
            case 1 -> new ResourceLocation(
                    "mowziesmobs", "sol_visage");
            case 2 -> new ResourceLocation(
                    "minecraft", "echo_shard");
            case 3 -> new ResourceLocation(
                    "legendary_monsters", "chorus_crystal");
            case 4 -> new ResourceLocation(
                    "goety", "shrouded_blueprint");
            case 5 -> new ResourceLocation(
                    "goety_revelation", "broken_halo");
            default -> null;
        };
    }

    public static Component composeHoverName(
            ItemStack stack,
            Component originalName
    ) {
        int level = getLevel(stack);

        if (level == 0) {
            return originalName;
        }

        String suffix = switch (level) {
            case 1 -> "blazing";
            case 2 -> "sculk";
            case 3 -> "ender";
            case 4 -> "void";
            case 5 -> "unholy";
            default -> "";
        };

        ChatFormatting color = switch (level) {
            case 1 -> ChatFormatting.GOLD;
            case 2 -> ChatFormatting.DARK_AQUA;
            case 3 -> ChatFormatting.DARK_PURPLE;
            case 4 -> ChatFormatting.LIGHT_PURPLE;
            case 5 -> ChatFormatting.DARK_RED;
            default -> ChatFormatting.WHITE;
        };

        Component prefix = Component.translatable(
                "whip_upgrade.until_eternity." + suffix
        ).withStyle(color);

        return Component.empty()
                .append(prefix)
                .append(" ")
                .append(originalName.copy());
    }
}
