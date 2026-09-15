package com.carrot123.until_eternity.client.render;

import com.carrot123.until_eternity.client.model.NetherworldKatanaReplacementModel;
import com.carrot123.until_eternity.until_eternity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class NetherworldKatanaClientEvents {
    private NetherworldKatanaClientEvents() {
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                NetherworldKatanaReplacementModel.LAYER_LOCATION,
                NetherworldKatanaReplacementModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onModelBakingCompleted(ModelEvent.BakingCompleted event) {
        NetherworldKatanaReplacementRenderer.invalidateModel();
    }
}
