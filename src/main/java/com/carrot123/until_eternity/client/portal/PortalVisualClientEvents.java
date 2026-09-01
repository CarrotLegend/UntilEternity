package com.carrot123.until_eternity.client.portal;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, value = Dist.CLIENT)
public final class PortalVisualClientEvents {
    private PortalVisualClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            PortalVisualTracker.updateFromVanillaEffect(
                    player.spinningEffectIntensity,
                    player.oSpinningEffectIntensity);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PortalVisualTracker.reset();
    }
}
