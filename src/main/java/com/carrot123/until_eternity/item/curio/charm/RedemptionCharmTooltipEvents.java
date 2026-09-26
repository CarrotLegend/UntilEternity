package com.carrot123.until_eternity.item.curio.charm;

import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RedemptionCharmTooltipEvents {
    private RedemptionCharmTooltipEvents() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(ModItems.REDEMPTION_STAR.get())) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.until_eternity.redemption_star.effect").withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ModItems.UNSTABLE_HALO.get())) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.until_eternity.unstable_halo.effect").withStyle(ChatFormatting.GRAY));
        } else if (stack.is(ModItems.KABBALAH_TREE.get())) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.until_eternity.kabbalah_tree.line1").withStyle(ChatFormatting.DARK_PURPLE));
            event.getToolTip().add(Component.translatable(
                    "tooltip.until_eternity.kabbalah_tree.line2").withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.translatable(
                    "tooltip.until_eternity.kabbalah_tree.line3").withStyle(ChatFormatting.GRAY));
        } else {
            return;
        }
    }
}
