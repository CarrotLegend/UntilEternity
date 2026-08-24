package com.carrot123.until_eternity.block;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("null")
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, until_eternity.MODID);

    public static final RegistryObject<Block> CHAOS_PORTAL = BLOCKS.register("chaos_portal",
            ChaosPortalBlock::new);

    public static final RegistryObject<Block> FINAL_KEY_MOLD = BLOCKS.register("final_key_mold",
            FinalKeyMoldBlock::new);

    public static final RegistryObject<Block> IMMORTAL_ALTAR = BLOCKS.register("immortal_altar",
            ImmortalAltarBlock::new);
    public static final RegistryObject<Block> END_CRAFTING_TABLE = BLOCKS.register("end_crafting_table",
            EndCraftingTableBlock::new);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
