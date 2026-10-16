package com.carrot123.until_eternity.trial;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TrialWeatherClientEvents {
    private TrialWeatherClientEvents() {}
    @SubscribeEvent public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.dimension().equals(TrialRealm.KEY)) {
            level.setRainLevel(0);
            level.setThunderLevel(0);
        }
    }
}
