package com.carrot123.until_eternity.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

public final class UniversalSmithingTemplateItem extends SmithingTemplateItem {
    public static final String DESCRIPTION_ID =
            "item.until_eternity.universal_smithing_template";
    public static final String APPLIES_TO_ID =
            "item.until_eternity.smithing_template.universal.applies_to";
    public static final String INGREDIENTS_ID =
            "item.until_eternity.smithing_template.universal.ingredients";
    public static final String UPGRADE_ID =
            "item.until_eternity.smithing_template.universal.upgrade";
    public static final String BASE_SLOT_DESCRIPTION_ID =
            "item.until_eternity.smithing_template.universal.base_slot_description";
    public static final String ADDITIONS_SLOT_DESCRIPTION_ID =
            "item.until_eternity.smithing_template.universal.additions_slot_description";

    private static final List<ResourceLocation> BASE_SLOT_EMPTY_ICONS = List.of(
            minecraftIcon("item/empty_armor_slot_helmet"),
            minecraftIcon("item/empty_slot_sword"),
            minecraftIcon("item/empty_armor_slot_chestplate"),
            minecraftIcon("item/empty_slot_pickaxe"),
            minecraftIcon("item/empty_armor_slot_leggings"),
            minecraftIcon("item/empty_slot_axe"),
            minecraftIcon("item/empty_armor_slot_boots"),
            minecraftIcon("item/empty_slot_hoe"),
            minecraftIcon("item/empty_slot_shovel")
    );
    private static final List<ResourceLocation> ADDITIONAL_SLOT_EMPTY_ICONS =
            List.of(minecraftIcon("item/empty_slot_ingot"));

    public UniversalSmithingTemplateItem() {
        super(
                Component.translatable(APPLIES_TO_ID).withStyle(ChatFormatting.BLUE),
                Component.translatable(INGREDIENTS_ID).withStyle(ChatFormatting.BLUE),
                Component.translatable(UPGRADE_ID).withStyle(ChatFormatting.GRAY),
                Component.translatable(BASE_SLOT_DESCRIPTION_ID),
                Component.translatable(ADDITIONS_SLOT_DESCRIPTION_ID),
                BASE_SLOT_EMPTY_ICONS,
                ADDITIONAL_SLOT_EMPTY_ICONS
        );
    }

    @Override
    public String getDescriptionId() {
        return DESCRIPTION_ID;
    }

    private static ResourceLocation minecraftIcon(String path) {
        return new ResourceLocation("minecraft", path);
    }
}
