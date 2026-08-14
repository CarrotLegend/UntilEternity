package com.carrot123.until_eternity.event;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.confluence.terra_curio.misc.ModAttributes;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class NetherworldKatanaTooltipEvents {
    private NetherworldKatanaTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!NetherworldKatanaEvents.isNetherworldKatana(
                event.getItemStack())) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        int automaticLine = findAutomaticCriticalChanceLine(tooltip);
        Component formattedCriticalChance = Component.translatable(
                "tooltip.until_eternity.netherworld_katana.critical_chance")
                .withStyle(ChatFormatting.BLUE);
        if (automaticLine >= 0) {
            tooltip.set(automaticLine, formattedCriticalChance);
        } else {
            tooltip.add(formattedCriticalChance);
        }
        tooltip.add(Component.translatable(
                "tooltip.until_eternity.netherworld_katana.immortal_scar")
                .withStyle(ChatFormatting.GRAY));
    }

    static int findAutomaticCriticalChanceLine(List<Component> tooltip) {
        String attributeKey =
                ModAttributes.getCriticalChance().getDescriptionId();
        for (int index = 0; index < tooltip.size(); index++) {
            if (isAutomaticCriticalChanceLine(
                    tooltip.get(index), attributeKey)) {
                return index;
            }
        }
        return -1;
    }

    private static boolean isAutomaticCriticalChanceLine(
            Component component,
            String attributeKey) {
        if (!(component.getContents()
                instanceof TranslatableContents line)
                || !"attribute.modifier.plus.0".equals(line.getKey())) {
            return false;
        }
        Object[] arguments = line.getArgs();
        if (arguments.length != 2
                || !"0.25".equals(String.valueOf(arguments[0]))
                || !(arguments[1] instanceof Component attributeName)
                || !(attributeName.getContents()
                instanceof TranslatableContents attribute)) {
            return false;
        }
        return attributeKey.equals(attribute.getKey());
    }
}
