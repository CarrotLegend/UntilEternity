package com.carrot123.until_eternity.block.entity;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, until_eternity.MODID);

    public static final RegistryObject<BlockEntityType<ImmortalAltarBlockEntity>> IMMORTAL_ALTAR =
            BLOCK_ENTITIES.register("immortal_altar",
                    () -> BlockEntityType.Builder.of(
                            ImmortalAltarBlockEntity::new,
                            ModBlocks.IMMORTAL_ALTAR.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
