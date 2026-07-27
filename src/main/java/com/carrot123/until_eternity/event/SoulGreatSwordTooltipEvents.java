package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.compat.legendarymonsters.SoulGreatSwordCompat;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, value = Dist.CLIENT)
public final class SoulGreatSwordTooltipEvents {
    private static final Set<String> UPSTREAM_ABILITY_LINES = Set.of(
            "item.legendary_monsters.soul_great_sword1",
            "item.legendary_monsters.soul_great_sword2",
            "item.legendary_monsters.soul_great_sword3",
            "item.legendary_monsters.soul_great_sword4",
            "item.legendary_monsters.soul_great_sword5"
    );

    private SoulGreatSwordTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!SoulGreatSwordCompat.isSoulGreatSword(event.getItemStack())) {
            return;
        }

        event.getToolTip().removeIf(component ->
                component.getContents() instanceof TranslatableContents translatable
                        && UPSTREAM_ABILITY_LINES.contains(translatable.getKey()));
        event.getToolTip().add(Component.translatable(
                "tooltip.until_eternity.soul_great_sword.bleeding").withStyle(ChatFormatting.GRAY));
        event.getToolTip().add(Component.translatable(
                "tooltip.until_eternity.soul_great_sword.rage").withStyle(ChatFormatting.GRAY));
        event.getToolTip().add(Component.translatable(
                "tooltip.until_eternity.soul_great_sword.rage_attributes").withStyle(ChatFormatting.GRAY));
    }
}
