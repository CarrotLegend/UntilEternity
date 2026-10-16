package com.carrot123.until_eternity.block.entity;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.carrot123.until_eternity.trial.TrialPortalBlockEntity;
import com.carrot123.until_eternity.trial.TrialMonumentBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, until_eternity.MODID);

    public static final RegistryObject<BlockEntityType<ImmortalAltarBlockEntity>> IMMORTAL_ALTAR =
            BLOCK_ENTITIES.register("immortal_altar",
                    () -> BlockEntityType.Builder.of(
                            ImmortalAltarBlockEntity::new,
                            ModBlocks.IMMORTAL_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<EndCraftingTableBlockEntity>> END_CRAFTING_TABLE =
            BLOCK_ENTITIES.register("end_crafting_table",
                    () -> BlockEntityType.Builder.of(
                            EndCraftingTableBlockEntity::new,
                            ModBlocks.END_CRAFTING_TABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<StormAltarBlockEntity>> STORM_ALTAR =
            BLOCK_ENTITIES.register("storm_altar", () -> BlockEntityType.Builder.of(
                    StormAltarBlockEntity::new, ModBlocks.STORM_ALTAR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TrialPortalBlockEntity>> TRIAL_PORTAL =
            BLOCK_ENTITIES.register("trial_portal", () -> BlockEntityType.Builder.of(
                    TrialPortalBlockEntity::new, ModBlocks.TRIAL_PORTAL.get()).build(null));
    public static final RegistryObject<BlockEntityType<TrialMonumentBlockEntity>> TRIAL_MONUMENT =
            BLOCK_ENTITIES.register("trial_monument", () -> BlockEntityType.Builder.of(
                    TrialMonumentBlockEntity::new, ModBlocks.TRIAL_MONUMENT.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
