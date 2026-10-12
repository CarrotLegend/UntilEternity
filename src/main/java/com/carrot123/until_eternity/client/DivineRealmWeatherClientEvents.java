package com.carrot123.until_eternity.client;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DivineRealmWeatherClientEvents {
    private static final ResourceKey<Level> DIVINE_REALM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(until_eternity.MODID, "divine_realm")
    );

    private DivineRealmWeatherClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && DIVINE_REALM.equals(level.dimension())) {
            level.setRainLevel(0.0F);
            level.setThunderLevel(0.0F);
        }
    }
}
