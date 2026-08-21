package com.carrot123.until_eternity.client.tooltip;

import com.carrot123.until_eternity.until_eternity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class FocusDamageTooltipEvents {
    private FocusDamageTooltipEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        List<net.minecraft.network.chat.Component> lines =
                event.getToolTip();
        for (int index = 0; index < lines.size(); index++) {
            lines.set(index, FocusDamageTooltipFormatter.format(
                    lines.get(index)));
        }
    }
}
