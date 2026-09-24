package com.carrot123.until_eternity.tarot;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shiroroku.tarotcards.Item.TarotItem;

public final class TarotCardHelper {
    public static final String REVERSED_TAG = "until_eternity:tarot_reversed";
    public static final TagKey<Item> TAROT_CARDS_TAG = ItemTags.create(
            new ResourceLocation("tarotcards", "tarot_cards"));

    private TarotCardHelper() {
    }

    public static boolean isTarotCard(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof TarotItem;
    }

    public static boolean isTaggedTarotCard(ItemStack stack) {
        return isTarotCard(stack) && stack.is(TAROT_CARDS_TAG);
    }

    public static ResourceLocation cardId(ItemStack stack) {
        return isTarotCard(stack) ? ForgeRegistries.ITEMS.getKey(stack.getItem()) : null;
    }

    public static TarotOrientation orientation(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(REVERSED_TAG)
                ? TarotOrientation.REVERSED : TarotOrientation.UPRIGHT;
    }

    public static Component composeHoverName(ItemStack stack, Component originalName) {
        if (!isTarotCard(stack)) {
            return originalName;
        }
        boolean reversed = orientation(stack) == TarotOrientation.REVERSED;
        Component baseName = stack.hasCustomHoverName()
                ? Component.literal(originalName.getString())
                : originalName.copy().setStyle(Style.EMPTY);
        return Component.empty().append(baseName).append(" · ")
                .append(Component.translatable(reversed
                        ? "tooltip.until_eternity.tarot.reversed"
                        : "tooltip.until_eternity.tarot.upright"))
                .withStyle(reversed ? ChatFormatting.DARK_PURPLE : ChatFormatting.GOLD);
    }

    public static void toggle(ItemStack stack) {
        if (isTarotCard(stack)) {
            stack.getOrCreateTag().putBoolean(REVERSED_TAG,
                    orientation(stack) == TarotOrientation.UPRIGHT);
        }
    }
}
