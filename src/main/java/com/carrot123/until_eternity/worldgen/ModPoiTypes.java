package com.carrot123.until_eternity.worldgen;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModPoiTypes {
    public static final ResourceKey<PoiType> CHAOS_PORTAL_KEY = ResourceKey.create(
            Registries.POINT_OF_INTEREST_TYPE,
            new ResourceLocation(until_eternity.MODID, "chaos_portal"));

    private static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, until_eternity.MODID);

    public static final RegistryObject<PoiType> CHAOS_PORTAL = POI_TYPES.register(
            "chaos_portal",
            () -> new PoiType(
                    ModBlocks.CHAOS_PORTAL.get().getStateDefinition().getPossibleStates().stream()
                            .collect(java.util.stream.Collectors.toSet()),
                    0,
                    1));

    private ModPoiTypes() {
    }

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
}
