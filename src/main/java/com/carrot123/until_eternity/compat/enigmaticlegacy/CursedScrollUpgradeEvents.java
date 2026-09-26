package com.carrot123.until_eternity.compat.enigmaticlegacy;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedScroll;
import com.carrot123.until_eternity.compat.goetyrevelation.BlessingScrollDamageCap;
import com.carrot123.until_eternity.compat.goetyrevelation.BlessingScrollTooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Locale;

public final class CursedScrollUpgradeEvents {
    private CursedScrollUpgradeEvents() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(
                CursedScrollUpgradeEvents.class
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(
            PlayerInteractEvent.RightClickItem event
    ) {
        if (ScrollUpgradeHelper.tryUpgrade(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(
                    event.getEntity().level().isClientSide));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (ScrollUpgradeHelper.tryUpgrade(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(
                    event.getEntity().level().isClientSide));
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (BlessingScrollDamageCap.isBlessingScroll(stack)) {
            BlessingScrollTooltips.append(stack, event.getToolTip());
            return;
        }

        if (!CursedScrollDamageCap.isCursedScroll(stack)) {
            return;
        }

        int tier =
                CursedScrollDamageCap.getTier(stack);

        List<Component> tooltip =
                event.getToolTip();

        tooltip.add(
                Component.empty()
        );

        if (tier
                >= CursedScrollDamageCap.UNLIMITED_TIER) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.until_eternity.cursed_scroll.damage_cap_unlimited"
                    ).withStyle(ChatFormatting.GOLD)
            );
        } else {
            tooltip.add(
                    Component.translatable(
                            "tooltip.until_eternity.cursed_scroll.damage_cap",
                            getCapText(tier)
                    ).withStyle(ChatFormatting.GOLD)
            );
        }

        if (tier
                < CursedScrollDamageCap.UNLIMITED_TIER) {
            int nextTier = tier + 1;

            ItemStack material =
                    CursedScrollDamageCap
                            .getMaterialStackForTier(nextTier);

            Component materialName =
                    material.isEmpty()
                            ? Component.literal(
                            String.valueOf(
                                    CursedScrollDamageCap
                                            .getMaterialForTier(
                                                    nextTier
                                            )
                            )
                    )
                            : material.getHoverName();

            if (nextTier
                    == CursedScrollDamageCap.UNLIMITED_TIER) {
                tooltip.add(
                        Component.translatable(
                                "tooltip.until_eternity.cursed_scroll.next_unlimited",
                                materialName
                        ).withStyle(ChatFormatting.GRAY)
                );
            } else {
                tooltip.add(
                        Component.translatable(
                                "tooltip.until_eternity.cursed_scroll.next_cap",
                                materialName,
                                getCapText(nextTier)
                        ).withStyle(ChatFormatting.GRAY)
                );
            }
        } else {
            tooltip.add(
                    Component.translatable(
                            "tooltip.until_eternity.cursed_scroll.fully_unlocked"
                    ).withStyle(ChatFormatting.DARK_PURPLE)
            );
        }

        Player player = event.getEntity();

        if (player == null
                || CursedScroll.damageBoost == null) {
            return;
        }

        replaceDisplayedDamageBoost(
                tooltip,
                player,
                stack
        );
    }

    private static void replaceDisplayedDamageBoost(
            List<Component> tooltip,
            Player player,
            ItemStack stack
    ) {
        boolean hasDynamicLine = false;

        for (Component component : tooltip) {
            if (component.getContents()
                    instanceof TranslatableContents contents
                    && "tooltip.enigmaticlegacy.cursed_scroll7"
                    .equals(contents.getKey())) {
                hasDynamicLine = true;
                break;
            }
        }

        if (!hasDynamicLine) {
            return;
        }

        ItemStack equipped =
                CursedScrollDamageCap.getEquippedScroll(player);

        if (equipped.isEmpty()) {
            return;
        }

        double rawBoost =
                CursedScrollDamageCap.getRawBoost(player);

        double effectiveBoost =
                CursedScrollDamageCap.clampBoost(
                        stack,
                        rawBoost
                );

        String value =
                formatPercentage(effectiveBoost) + "%";

        for (int index = 0;
             index < tooltip.size();
             index++) {
            Component original =
                    tooltip.get(index);

            if (!(original.getContents()
                    instanceof TranslatableContents contents)
                    || !"tooltip.enigmaticlegacy.cursed_scroll7"
                    .equals(contents.getKey())) {
                continue;
            }

            MutableComponent replacement =
                    Component.translatable(
                            "tooltip.enigmaticlegacy.cursed_scroll7",
                            value
                    ).setStyle(original.getStyle());

            original.getSiblings().forEach(
                    replacement::append
            );

            tooltip.set(
                    index,
                    replacement
            );
        }
    }

    private static String getCapText(int tier) {
        return switch (tier) {
            case CursedScrollDamageCap.ABYSSAL_TIER ->
                    "400%";
            case CursedScrollDamageCap.DIVINE_TIER ->
                    "600%";
            case CursedScrollDamageCap.MONSTROUS_TIER ->
                    "800%";
            case CursedScrollDamageCap.UNLIMITED_TIER ->
                    "∞";
            default ->
                    "200%";
        };
    }

    private static String formatPercentage(
            double modifier
    ) {
        double percentage = modifier * 100.0D;

        if (Math.abs(
                percentage - Math.rint(percentage)
        ) < 0.000001D) {
            return Long.toString(
                    Math.round(percentage)
            );
        }

        String formatted =
                String.format(
                        Locale.ROOT,
                        "%.2f",
                        percentage
                );

        while (formatted.endsWith("0")) {
            formatted =
                    formatted.substring(
                            0,
                            formatted.length() - 1
                    );
        }

        if (formatted.endsWith(".")) {
            formatted =
                    formatted.substring(
                            0,
                            formatted.length() - 1
                    );
        }

        return formatted;
    }
}
