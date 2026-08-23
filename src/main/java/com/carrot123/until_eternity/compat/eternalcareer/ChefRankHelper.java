package com.carrot123.until_eternity.compat.eternalcareer;

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

    public static final String RANK_TAG =
            "until_eternity:chef_rank";

    public static final String ETERNAL_CAREER_MOD_ID =
            "eternal_career";

    public static final ResourceLocation KITCHENWARE_DAMAGE_ID =
            new ResourceLocation(
                    "eternal_career",
                    "kitchenware_damage"
            );

    private static final Set<ResourceLocation> CHEF_ARMOR_IDS =
            Set.of(
                    id("chef_hat"),
                    id("chef_jacket"),
                    id("chef_leggings"),
                    id("chef_boots")
            );

    private static final ResourceLocation APPRENTICE_BADGE =
            id("chef_apprentice_badge");

    private static final ResourceLocation INTERMEDIATE_BADGE =
            id("intermediate_chef_badge");

    private static final ResourceLocation ADVANCED_BADGE =
            id("advanced_chef_badge");

    private static final ResourceLocation SENIOR_BADGE =
            id("senior_technician_badge");

    private static final ResourceLocation MASTER_BADGE =
            id("master_chef_badge");

    private ChefRankHelper() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(
                ETERNAL_CAREER_MOD_ID,
                path
        );
    }

    public static boolean isChefArmor(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation itemId =
                ForgeRegistries.ITEMS.getKey(
                        stack.getItem()
                );

        return itemId != null
                && CHEF_ARMOR_IDS.contains(itemId);
    }

    public static ChefRank getRank(ItemStack stack) {

        if (!isChefArmor(stack)) {
            return ChefRank.NONE;
        }

        if (stack.getTag() == null) {
            return ChefRank.NONE;
        }

        if (!stack.getTag().contains(
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
            ItemStack stack
    ) {

        if (stack == null || stack.isEmpty()) {
            return ChefRank.NONE;
        }

        ResourceLocation itemId =
                ForgeRegistries.ITEMS.getKey(
                        stack.getItem()
                );

        if (itemId == null) {
            return ChefRank.NONE;
        }

        if (itemId.equals(APPRENTICE_BADGE)) {
            return ChefRank.APPRENTICE;
        }

        if (itemId.equals(INTERMEDIATE_BADGE)) {
            return ChefRank.INTERMEDIATE;
        }

        if (itemId.equals(ADVANCED_BADGE)) {
            return ChefRank.ADVANCED;
        }

        if (itemId.equals(SENIOR_BADGE)) {
            return ChefRank.SENIOR;
        }

        if (itemId.equals(MASTER_BADGE)) {
            return ChefRank.MASTER;
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

        if (target == ChefRank.NONE) {
            return false;
        }

        return target.id()
                == current.id() + 1;
    }

    public static Component composeHoverName(
            ItemStack stack,
            Component originalName
    ) {

        ChefRank rank =
                getRank(stack);

        if (rank == ChefRank.NONE) {
            return originalName;
        }

        Component prefix =
                Component.translatable(
                        rank.translationKey()
                );

        if (rank.color() != null) {
            prefix =
                    prefix.copy()
                            .withStyle(rank.color());
        }

        return Component.empty()
                .append(prefix)
                .append(" ")
                .append(originalName.copy());
    }

    public static UUID getOriginalChefModifierId(
            EquipmentSlot slot,
            String attributePath
    ) {

        String key =
                "eternal_career:chef_armor/"
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