package com.carrot123.until_eternity.client.tooltip;

import com.carrot123.until_eternity.tarot.TarotCardHelper;
import com.carrot123.until_eternity.tarot.TarotCardRequirement;
import com.carrot123.until_eternity.tarot.TarotDeckScanner;
import com.carrot123.until_eternity.tarot.TarotOrientation;
import com.carrot123.until_eternity.tarot.TarotSetDefinition;
import com.carrot123.until_eternity.tarot.TarotSetMatcher;
import com.carrot123.until_eternity.tarot.TarotSetRegistry;
import com.carrot123.until_eternity.until_eternity;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TarotTooltipEvents {
    private TarotTooltipEvents() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (TarotCardHelper.isTarotCard(stack)) {
            addCardTooltip(stack, event.getToolTip());
        } else if (TarotDeckScanner.isDeck(stack)) {
            addDeckTooltip(stack, event.getToolTip());
        }
    }

    private static void addCardTooltip(ItemStack stack, List<Component> lines) {
        TarotOrientation current = TarotCardHelper.orientation(stack);
        String key = current == TarotOrientation.REVERSED
                ? "tooltip.until_eternity.tarot.orientation.reversed"
                : "tooltip.until_eternity.tarot.orientation.upright";
        lines.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.until_eternity.tarot.available_sets")
                .withStyle(ChatFormatting.GRAY));
        ResourceLocation cardId = TarotCardHelper.cardId(stack);
        addCardDirection(lines, cardId, TarotOrientation.UPRIGHT, current);
        addCardDirection(lines, cardId, TarotOrientation.REVERSED, current);
    }

    private static void addCardDirection(List<Component> lines, ResourceLocation cardId,
            TarotOrientation direction, TarotOrientation current) {
        boolean selected = direction == current;
        boolean upright = direction == TarotOrientation.UPRIGHT;
        String heading = upright
                ? "tooltip.until_eternity.tarot.upright_sets"
                : "tooltip.until_eternity.tarot.reversed_sets";
        ChatFormatting headingColor = selected
                ? (upright ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE)
                : ChatFormatting.DARK_GRAY;
        ChatFormatting nameColor = selected
                ? (upright ? ChatFormatting.YELLOW : ChatFormatting.LIGHT_PURPLE)
                : ChatFormatting.GRAY;
        lines.add(Component.translatable(heading).withStyle(headingColor));
        boolean found = false;
        for (TarotSetDefinition definition : TarotSetRegistry.definitions()) {
            TarotCardRequirement requirement = cardId == null ? null
                    : definition.requiredCards().get(cardId);
            if (requirement != null && requirement.accepts(direction)) {
                lines.add(Component.literal("  ").append(
                        Component.translatable(definition.getNameTranslationKey())
                                .withStyle(nameColor)));
                found = true;
            }
        }
        if (!found) {
            lines.add(Component.literal("  ").append(
                    Component.translatable("tooltip.until_eternity.tarot.none")
                            .withStyle(ChatFormatting.GRAY)));
        }
    }

    private static void addDeckTooltip(ItemStack stack, List<Component> lines) {
        Set<ResourceLocation> matched = TarotSetMatcher.match(TarotDeckScanner.scan(stack),
                TarotSetRegistry.definitions());
        lines.add(Component.translatable("tooltip.until_eternity.tarot.activated_sets")
                .withStyle(ChatFormatting.GRAY));
        if (matched.isEmpty()) {
            lines.add(Component.translatable("tooltip.until_eternity.tarot.none")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        for (TarotSetDefinition definition : TarotSetRegistry.definitions()) {
            if (!matched.contains(definition.id())) {
                continue;
            }
            lines.add(Component.translatable(definition.getNameTranslationKey())
                    .withStyle(ChatFormatting.GOLD));
            for (String descriptionKey : definition.getDescriptionTranslationKeys()) {
                lines.add(Component.literal("  ").append(
                        Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY)));
            }
        }
    }
}
