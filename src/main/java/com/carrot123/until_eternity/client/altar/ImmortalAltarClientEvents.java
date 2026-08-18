package com.carrot123.until_eternity.client.altar;

import com.carrot123.until_eternity.block.entity.ModBlockEntities;
import com.carrot123.until_eternity.until_eternity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = until_eternity.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ImmortalAltarClientEvents {
    private ImmortalAltarClientEvents() {
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                ImmortalAltarModel.LAYER_LOCATION,
                ImmortalAltarModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.IMMORTAL_ALTAR.get(),
                ImmortalAltarBlockEntityRenderer::new);
    }
}
