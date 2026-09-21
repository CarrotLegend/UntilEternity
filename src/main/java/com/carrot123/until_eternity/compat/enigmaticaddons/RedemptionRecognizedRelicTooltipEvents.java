package com.carrot123.until_eternity.compat.enigmaticaddons;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class RedemptionRecognizedRelicTooltipEvents {

    private static final ResourceLocation NIGHT_SCROLL =
            new ResourceLocation(
                    "enigmaticaddons",
                    "night_scroll"
            );

    private static final ResourceLocation BERSERK_CHARM =
            new ResourceLocation(
                    "enigmaticlegacy",
                    "berserk_charm"
            );

    private RedemptionRecognizedRelicTooltipEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(
            ItemTooltipEvent event
    ) {
        if (event.getEntity() == null) {
            return;
        }

        ResourceLocation itemId =
                ForgeRegistries.ITEMS.getKey(
                        event.getItemStack().getItem()
                );

        if (!NIGHT_SCROLL.equals(itemId)
                && !BERSERK_CHARM.equals(itemId)) {
            return;
        }

        if (!RedemptionRecognitionCompat.hasBlessStatus(
                event.getEntity()
        )) {
            return;
        }

        event.getToolTip().replaceAll(component -> {
            if (!(component.getContents()
                    instanceof TranslatableContents contents)) {
                return component;
            }

            return switch (contents.getKey()) {
                case "tooltip.enigmaticlegacy.cursedOnesOnly1" ->
                        Component.translatable(
                                "tooltip.enigmaticaddons.blessTrueUse1"
                        ).withStyle(
                                ChatFormatting.GOLD
                        );

                case "tooltip.enigmaticlegacy.cursedOnesOnly2" ->
                        Component.translatable(
                                "tooltip.enigmaticaddons.blessTrueUse2"
                        ).withStyle(
                                ChatFormatting.GOLD
                        );

                default -> component;
            };
        });
    }
}