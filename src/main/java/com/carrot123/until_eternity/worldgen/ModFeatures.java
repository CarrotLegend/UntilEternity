package com.carrot123.until_eternity.worldgen;

import com.carrot123.until_eternity.until_eternity;
import com.carrot123.until_eternity.worldgen.feature.ChaosTreeFeature;
import com.carrot123.until_eternity.worldgen.feature.StructureTemplateFeature;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, until_eternity.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CHAOS_TREE =
            FEATURES.register("chaos_tree",
                    () -> new ChaosTreeFeature(NoneFeatureConfiguration.CODEC));
    
    public static final RegistryObject<Feature<?>> STATUE =
            FEATURES.register(
                    "statue",
                    () -> new StructureTemplateFeature(
                            new ResourceLocation(
                                    until_eternity.MODID,
                                    "statue"
                            )
                    )
            );

    public static final RegistryObject<Feature<?>> BROKEN_STATUE =
            FEATURES.register(
                    "broken_statue",
                    () -> new StructureTemplateFeature(
                            new ResourceLocation(
                                    until_eternity.MODID,
                                    "broken_statue"
                            )
                    )
            );

    private ModFeatures() {
    }

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
