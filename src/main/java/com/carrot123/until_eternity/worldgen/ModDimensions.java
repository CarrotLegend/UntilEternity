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

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModDimensions {

    public static final ResourceLocation CHAOS_REALM_EFFECTS =
            new ResourceLocation(until_eternity.MODID + ":chaos_realm");

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(CHAOS_REALM_EFFECTS, new ChaosRealmEffects());
    }

    /**
     * Custom sky/fog effects for the Chaos Realm.
     * Sky is black/no color, always midnight moon visible.
     */
    public static class ChaosRealmEffects extends DimensionSpecialEffects {

        public ChaosRealmEffects() {
            super(Float.NaN, // cloud height — NaN disables clouds
                    false, // no ground level fog
                    SkyType.NORMAL,
                    false, // no force bright lightmap
                    false); // no constant ambient light
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight) {
            // Always dark like midnight — fog matches the dark sky
            return new Vec3(0.0, 0.0, 0.0);
        }

        @Override
        public boolean isFoggyAt(int x, int y) {
            return false;
        }

        @Nullable
        @Override
        public float[] getSunriseColor(float dayTime, float partialTick) {
            return null; // No sunrise/sunset color; always dark
        }
    }
}
