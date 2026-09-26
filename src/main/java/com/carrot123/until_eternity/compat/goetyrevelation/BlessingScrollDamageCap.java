package com.carrot123.until_eternity.compat.goetyrevelation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

public final class BlessingScrollDamageCap {
    public static final String CAP_TIER_TAG =
            "until_eternity:BlessingScrollDamageCapTier";
    public static final int DEFAULT_TIER = 0;
    public static final int DRAGONSKIN_TIER = 1;
    public static final int BLUEPRINT_TIER = 2;
    public static final int UNLIMITED_TIER = 3;
    public static final double DEFAULT_CAP = 2.0D;
    public static final double DRAGONSKIN_CAP = 4.0D;
    public static final double BLUEPRINT_CAP = 6.0D;

    public static final ResourceLocation BLESSING_SCROLL =
            new ResourceLocation("goety_revelation", "blessing_scroll");
    public static final ResourceLocation DRAGONSKIN =
            new ResourceLocation("irons_spellbooks", "dragonskin");
    public static final ResourceLocation SHROUDED_BLUEPRINT =
            new ResourceLocation("goety", "shrouded_blueprint");
    public static final ResourceLocation UNHOLY_BLOOD =
            new ResourceLocation("goety", "unholy_blood");

    private BlessingScrollDamageCap() {
    }

    public static boolean isBlessingScroll(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && BLESSING_SCROLL.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    public static int getTier(ItemStack stack) {
        if (!isBlessingScroll(stack) || stack.getTag() == null) {
            return DEFAULT_TIER;
        }
        return readTier(stack.getTag());
    }

    public static void setTier(ItemStack stack, int tier) {
        if (isBlessingScroll(stack)) {
            writeTier(stack.getOrCreateTag(), tier);
        }
    }

    public static int readTier(CompoundTag tag) {
        return tag == null ? DEFAULT_TIER : Mth.clamp(
                tag.getInt(CAP_TIER_TAG), DEFAULT_TIER, UNLIMITED_TIER);
    }

    public static void writeTier(CompoundTag tag, int tier) {
        tag.putInt(CAP_TIER_TAG, Mth.clamp(tier, DEFAULT_TIER, UNLIMITED_TIER));
    }

    public static boolean isUnlimited(ItemStack stack) {
        return getTier(stack) == UNLIMITED_TIER;
    }

    public static double getCap(ItemStack stack) {
        return getCapForTier(getTier(stack));
    }

    public static double getCapForTier(int tier) {
        return switch (tier) {
            case DRAGONSKIN_TIER -> DRAGONSKIN_CAP;
            case BLUEPRINT_TIER -> BLUEPRINT_CAP;
            case UNLIMITED_TIER -> Double.POSITIVE_INFINITY;
            default -> DEFAULT_CAP;
        };
    }

    public static double clampBoost(ItemStack stack, double rawBoost) {
        return clampBoostForTier(getTier(stack), rawBoost);
    }

    public static double clampBoostForTier(int tier, double rawBoost) {
        if (!Double.isFinite(rawBoost) || rawBoost <= 0.0D) {
            return rawBoost;
        }
        return Math.min(rawBoost, getCapForTier(tier));
    }

    public static ItemStack getEquippedScroll(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findCurios(BlessingScrollDamageCap::isBlessingScroll)
                        .stream()
                        .map(result -> result.stack())
                        .findFirst()
                        .orElse(ItemStack.EMPTY))
                .orElse(ItemStack.EMPTY);
    }

    public static int getUpgradeTier(ItemStack material) {
        if (material == null || material.isEmpty()) {
            return -1;
        }
        return getUpgradeTier(ForgeRegistries.ITEMS.getKey(material.getItem()));
    }

    public static int getUpgradeTier(ResourceLocation id) {
        if (DRAGONSKIN.equals(id)) {
            return DRAGONSKIN_TIER;
        }
        if (SHROUDED_BLUEPRINT.equals(id)) {
            return BLUEPRINT_TIER;
        }
        if (UNHOLY_BLOOD.equals(id)) {
            return UNLIMITED_TIER;
        }
        return -1;
    }

    public static boolean canUpgrade(int currentTier, int targetTier) {
        return targetTier > currentTier && targetTier <= UNLIMITED_TIER;
    }

    public static ResourceLocation getMaterialForTier(int tier) {
        return switch (tier) {
            case DRAGONSKIN_TIER -> DRAGONSKIN;
            case BLUEPRINT_TIER -> SHROUDED_BLUEPRINT;
            case UNLIMITED_TIER -> UNHOLY_BLOOD;
            default -> null;
        };
    }

    public static ItemStack getMaterialStackForTier(int tier) {
        ResourceLocation id = getMaterialForTier(tier);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static double capOriginalAmount(ItemStack scroll,
                                           double before, double originalAfter) {
        return capOriginalAmountForTier(getTier(scroll), before, originalAfter);
    }

    public static double capOriginalAmountForTier(int tier,
                                                  double before, double originalAfter) {
        if (!Double.isFinite(before) || before == 0.0D
                || !Double.isFinite(originalAfter)) {
            return originalAfter;
        }
        double rawBoost = originalAfter / before - 1.0D;
        if (!Double.isFinite(rawBoost) || rawBoost <= 0.0D) {
            return originalAfter;
        }
        double cappedBoost = clampBoostForTier(tier, rawBoost);
        if (cappedBoost == rawBoost) {
            return originalAfter;
        }
        double result = before * (1.0D + cappedBoost);
        return Double.isFinite(result) ? result : originalAfter;
    }

    public static double capDisplayedPercent(ItemStack scroll, double originalPercent) {
        if (!Double.isFinite(originalPercent) || originalPercent <= 0.0D) {
            return originalPercent;
        }
        return clampBoost(scroll, originalPercent / 100.0D) * 100.0D;
    }
}
