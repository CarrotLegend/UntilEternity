package com.carrot123.until_eternity.client.entity;

import com.carrot123.until_eternity.entity.StormPriestessEntity;
import com.carrot123.until_eternity.registry.ModEntities;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StormPriestessRenderer extends HumanoidMobRenderer<StormPriestessEntity, HumanoidModel<StormPriestessEntity>> {
    public StormPriestessRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(StormPriestessEntity entity) {
        return DefaultPlayerSkin.getDefaultSkin();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.STORM_PRIESTESS.get(), StormPriestessRenderer::new);
    }
}
