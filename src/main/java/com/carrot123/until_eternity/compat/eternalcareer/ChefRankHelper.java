package com.carrot123.until_eternity.compat.eternalcareer;

import com.carrot123.until_eternity.util.RainbowTextHelper;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;

public final class ChefRankHelper {

    public static final String RANK_TAG = "until_eternity:chef_rank";
    public static final String ETERNAL_CAREER_MOD_ID = "eternal_career";

    public static final ResourceLocation KITCHENWARE_DAMAGE_ID =
            new ResourceLocation(ETERNAL_CAREER_MOD_ID, "kitchenware_damage");

    private static final Set<String> CHEF_ARMOR_PATHS =
            Set.of(
                    "chef_hat",
                    "chef_jacket",
                    "chef_leggings",
                    "chef_boots"
            );

    private ChefRankHelper() {
    }

    public static boolean isChefArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation id =
                ForgeRegistries.ITEMS.getKey(
                        stack.getItem()
                );

        return id != null
                && ETERNAL_CAREER_MOD_ID.equals(
                        id.getNamespace()
                )
                && CHEF_ARMOR_PATHS.contains(
                        id.getPath()
                );
    }

    public static ChefRank getRank(ItemStack stack) {
        if (!isChefArmor(stack)) {
            return ChefRank.NONE;
        }

        if (stack.getTag() == null
                || !stack.getTag().contains(
                RANK_TAG,
                Tag.TAG_INT
        )) {
            return ChefRank.NONE;
        }

        return ChefRank.byId(
                stack.getTag().getInt(RANK_TAG)
        );
    }

    public static void setRank(
            ItemStack stack,
            ChefRank rank
    ) {
        if (!isChefArmor(stack)) {
            return;
        }

        if (rank == null || rank == ChefRank.NONE) {
            if (stack.getTag() != null) {
                stack.getTag().remove(RANK_TAG);
            }
            return;
        }

        stack.getOrCreateTag().putInt(
                RANK_TAG,
                rank.id()
        );
    }

    public static ChefRank getRankForBadge(
            ItemStack badge
    ) {
        if (badge == null || badge.isEmpty()) {
            return ChefRank.NONE;
        }

        ResourceLocation id =
                ForgeRegistries.ITEMS.getKey(
                        badge.getItem()
                );

        if (id == null
                || !ETERNAL_CAREER_MOD_ID.equals(
                id.getNamespace()
        )) {
            return ChefRank.NONE;
        }

        for (ChefRank rank : ChefRank.values()) {
            if (!rank.isRanked()
                    || rank.badgePath() == null) {
                continue;
            }

            if (rank.badgePath().equals(
                    id.getPath()
            )) {
                return rank;
            }
        }

        return ChefRank.NONE;
    }

    public static boolean canUpgrade(
            ItemStack armor,
            ItemStack badge
    ) {
        if (!isChefArmor(armor)) {
            return false;
        }

        ChefRank current =
                getRank(armor);

        ChefRank target =
                getRankForBadge(badge);

        if (!target.isRanked()) {
            return false;
        }

        return target.previous() == current;
    }

    public static Component composeHoverName(
            ItemStack stack,
            Component originalName
    ) {
        ChefRank rank = getRank(stack);

        if (!rank.isRanked()) {
            return originalName;
        }

        Component prefix;

        if (rank == ChefRank.MASTER) {
            String translated =
                    Component.translatable(
                            rank.translationKey()
                    ).getString();

            prefix = RainbowTextHelper.parse(translated);

        } else {
            prefix = Component.translatable(rank.translationKey()).copy().withStyle(rank.color());
        }

        return Component.empty()
                .append(prefix)
                .append(Component.literal(" "))
                .append(originalName.copy());
    }

    public static UUID getOriginalChefModifierId(
            EquipmentSlot slot,
            String attributePath
    ) {
        String key =
                ETERNAL_CAREER_MOD_ID
                        + ":chef_armor/"
                        + slot.getName()
                        + "/"
                        + attributePath;

        return UUID.nameUUIDFromBytes(
                key.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    public static UUID getRankModifierId(
            EquipmentSlot slot,
            String attributePath
    ) {
        String key =
                "until_eternity:chef_rank/"
                        + slot.getName()
                        + "/"
                        + attributePath;

        return UUID.nameUUIDFromBytes(
                key.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }
}