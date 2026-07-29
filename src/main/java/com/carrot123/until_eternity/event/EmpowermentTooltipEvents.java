package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.enchantment.EmpowermentLevel;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class EmpowermentTooltipEvents {
    private EmpowermentTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (EmpowermentLevel.read(event.getItemStack()) <= 0) {
            return;
        }
        event.getToolTip().add(Component.translatable(
                "tooltip.until_eternity.empowerment.description")
                .withStyle(ChatFormatting.GRAY));
        event.getToolTip().add(Component.translatable(
                "tooltip.until_eternity.empowerment.limit")
                .withStyle(ChatFormatting.GRAY));
    }
}
