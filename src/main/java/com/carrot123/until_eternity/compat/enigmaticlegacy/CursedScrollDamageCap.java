package com.carrot123.until_eternity.compat.enigmaticlegacy;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedScroll;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class CursedScrollDamageCap {
    public static final String CAP_TIER_TAG =
            "until_eternity:CursedScrollDamageCapTier";

    public static final int DEFAULT_TIER = 0;
    public static final int ABYSSAL_TIER = 1;
    public static final int DIVINE_TIER = 2;
    public static final int MONSTROUS_TIER = 3;
    public static final int UNLIMITED_TIER = 4;

    public static final double DEFAULT_CAP = 2.0D;
    public static final double ABYSSAL_CAP = 4.0D;
    public static final double DIVINE_CAP = 6.0D;
    public static final double MONSTROUS_CAP = 8.0D;

    public static final ResourceLocation CURSED_SCROLL =
            new ResourceLocation("enigmaticlegacy", "cursed_scroll");

    public static final ResourceLocation ABYSSAL_EGG =
            new ResourceLocation("cataclysm", "abyssal_egg");

    public static final ResourceLocation DIVINE_SOULSHARD =
            new ResourceLocation("irons_spellbooks", "divine_soulshard");

    public static final ResourceLocation MONSTROUS_HORN =
            new ResourceLocation("cataclysm", "monstrous_horn");

    public static final ResourceLocation UNHOLY_BLOOD =
            new ResourceLocation("goety", "unholy_blood");

    private CursedScrollDamageCap() {
    }

    public static boolean isCursedScroll(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation id =
                ForgeRegistries.ITEMS.getKey(stack.getItem());

        return CURSED_SCROLL.equals(id);
    }

    public static int getTier(ItemStack stack) {
        if (!isCursedScroll(stack) || stack.getTag() == null) {
            return DEFAULT_TIER;
        }

        return Mth.clamp(
                stack.getTag().getInt(CAP_TIER_TAG),
                DEFAULT_TIER,
                UNLIMITED_TIER
        );
    }

    public static void setTier(ItemStack stack, int tier) {
        if (!isCursedScroll(stack)) {
            return;
        }

        stack.getOrCreateTag().putInt(
                CAP_TIER_TAG,
                Mth.clamp(
                        tier,
                        DEFAULT_TIER,
                        UNLIMITED_TIER
                )
        );
    }

    public static boolean isUnlimited(ItemStack stack) {
        return getTier(stack) >= UNLIMITED_TIER;
    }

    public static double getCap(ItemStack stack) {
        return switch (getTier(stack)) {
            case ABYSSAL_TIER -> ABYSSAL_CAP;
            case DIVINE_TIER -> DIVINE_CAP;
            case MONSTROUS_TIER -> MONSTROUS_CAP;
            case UNLIMITED_TIER -> Double.POSITIVE_INFINITY;
            default -> DEFAULT_CAP;
        };
    }

    public static double clampBoost(
            ItemStack stack,
            double rawBoost
    ) {
        if (!Double.isFinite(rawBoost) || rawBoost <= 0.0D) {
            return 0.0D;
        }

        if (isUnlimited(stack)) {
            return rawBoost;
        }

        return Math.min(rawBoost, getCap(stack));
    }

    public static double getRawBoost(Player player) {
        if (player == null || CursedScroll.damageBoost == null) {
            return 0.0D;
        }

        double perCurse =
                CursedScroll.damageBoost
                        .getValue()
                        .asModifier();

        int curseCount =
                SuperpositionHandler.getCurseAmount(player);

        if (!Double.isFinite(perCurse)
                || perCurse <= 0.0D
                || curseCount <= 0) {
            return 0.0D;
        }

        double result = perCurse * curseCount;

        return Double.isFinite(result) && result > 0.0D
                ? result
                : 0.0D;
    }

    public static double getEffectiveBoost(
            Player player,
            ItemStack scroll
    ) {
        return clampBoost(scroll, getRawBoost(player));
    }

    public static ItemStack getEquippedScroll(Player player) {
        if (player == null || EnigmaticItems.CURSED_SCROLL == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack =
                SuperpositionHandler.getCurioStack(
                        player,
                        EnigmaticItems.CURSED_SCROLL
                );

        return stack == null ? ItemStack.EMPTY : stack;
    }

    public static int getUpgradeTier(ItemStack material) {
        if (material == null || material.isEmpty()) {
            return -1;
        }

        ResourceLocation id =
                ForgeRegistries.ITEMS.getKey(material.getItem());

        if (ABYSSAL_EGG.equals(id)) {
            return ABYSSAL_TIER;
        }

        if (DIVINE_SOULSHARD.equals(id)) {
            return DIVINE_TIER;
        }

        if (MONSTROUS_HORN.equals(id)) {
            return MONSTROUS_TIER;
        }

        if (UNHOLY_BLOOD.equals(id)) {
            return UNLIMITED_TIER;
        }

        return -1;
    }

    public static ResourceLocation getMaterialForTier(int tier) {
        return switch (tier) {
            case ABYSSAL_TIER -> ABYSSAL_EGG;
            case DIVINE_TIER -> DIVINE_SOULSHARD;
            case MONSTROUS_TIER -> MONSTROUS_HORN;
            case UNLIMITED_TIER -> UNHOLY_BLOOD;
            default -> null;
        };
    }

    public static ItemStack getMaterialStackForTier(int tier) {
        ResourceLocation id = getMaterialForTier(tier);

        if (id == null) {
            return ItemStack.EMPTY;
        }

        Item item = ForgeRegistries.ITEMS.getValue(id);

        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item);
    }
}