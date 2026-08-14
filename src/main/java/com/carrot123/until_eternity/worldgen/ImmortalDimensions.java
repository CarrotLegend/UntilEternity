package com.carrot123.until_eternity.worldgen;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class ImmortalDimensions {
    public static final ResourceLocation IMMORTAL_ID =
            new ResourceLocation(until_eternity.MODID, "immortal");
    public static final ResourceLocation IMMORTAL_WASTELAND_ID =
            new ResourceLocation(until_eternity.MODID,
                    "immortal_wasteland");

    public static final ResourceKey<Level> IMMORTAL_DIMENSION =
            ResourceKey.create(Registries.DIMENSION, IMMORTAL_ID);
    public static final ResourceKey<Biome> IMMORTAL_WASTELAND =
            ResourceKey.create(Registries.BIOME, IMMORTAL_WASTELAND_ID);

    private ImmortalDimensions() {
    }
}
