package com.carrot123.until_eternity.worldgen;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModDimensions {

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
            new ResourceLocation(until_eternity.MODID + ":chaos_realm"),
            new ChaosRealmEffects()
        );
    }

    public static class ChaosRealmEffects extends DimensionSpecialEffects {

        public ChaosRealmEffects() {
            super(Float.NaN, false, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
            return fogColor.multiply(brightness * 0.5F + 0.1F, brightness * 0.5F + 0.1F, brightness * 0.6F + 0.15F);
        }

        @Override
        public boolean isFoggyAt(int camX, int camY) {
            return false;
        }
    }
}
