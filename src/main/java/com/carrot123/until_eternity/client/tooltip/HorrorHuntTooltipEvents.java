package com.carrot123.until_eternity.client.tooltip;

import com.mojang.datafixers.util.Either;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.until_eternity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class HorrorHuntTooltipEvents {
    private HorrorHuntTooltipEvents() {
    }

    @Mod.EventBusSubscriber(
            modid = until_eternity.MODID,
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT)
    public static final class ForgeEvents {
        private ForgeEvents() {
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onGatherTooltip(
                RenderTooltipEvent.GatherComponents event
        ) {
            if (!event.getItemStack().is(ModItems.HORROR_HUNT.get())
                    || event.getTooltipElements().isEmpty()) {
                return;
            }
            event.getTooltipElements().get(0).left().ifPresent(text ->
                    event.getTooltipElements().set(0, Either.right(
                            new HorrorHuntTooltipComponent(text))));
        }
    }

    @Mod.EventBusSubscriber(
            modid = until_eternity.MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerTooltipFactory(
                RegisterClientTooltipComponentFactoriesEvent event
        ) {
            event.register(
                    HorrorHuntTooltipComponent.class,
                    ClientHorrorHuntTooltipComponent::new);
        }
    }
}
