package com.carrot123.until_eternity.client.entity;

import com.carrot123.until_eternity.entity.StormPriestessEntity;
import com.carrot123.until_eternity.registry.ModEntities;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StormPriestessRenderer extends GeoEntityRenderer<StormPriestessEntity> {
    public StormPriestessRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
                new ResourceLocation(until_eternity.MODID, "storm_priestess"), true));
        this.shadowRadius = 0.45F;
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.STORM_PRIESTESS.get(), StormPriestessRenderer::new);
    }
}
