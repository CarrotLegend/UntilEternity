package com.carrot123.until_eternity.worldgen.structure;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructureTypes {
    private static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, until_eternity.MODID);

    public static final RegistryObject<StructureType<StableEndJigsawStructure>> STABLE_END_JIGSAW =
            TYPES.register("stable_end_jigsaw", () -> () -> StableEndJigsawStructure.CODEC);

    private ModStructureTypes() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}
