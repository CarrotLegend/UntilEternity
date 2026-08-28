package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.enchantment.ActualEnchantmentLevel;
import com.carrot123.until_eternity.enchantment.ModEnchantments;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class UselessCurseTooltipEvents {
    private UselessCurseTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!ModEnchantments.USELESS_CURSE.isPresent()
                || ActualEnchantmentLevel.read(
                        ModEnchantments.USELESS_CURSE.get(),
                        event.getItemStack()) <= 0) {
            return;
        }
        event.getToolTip().add(Component.translatable(
                        "enchantment.until_eternity.useless_curse.desc")
                .withStyle(ChatFormatting.GRAY));
    }
}
