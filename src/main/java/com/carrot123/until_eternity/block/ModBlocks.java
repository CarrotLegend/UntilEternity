package com.carrot123.until_eternity.block;

import com.carrot123.until_eternity.until_eternity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@SuppressWarnings("null")
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, until_eternity.MODID);

    public static final RegistryObject<Block> CHAOS_PORTAL = registerBlock("chaos_portal",
            () -> new ChaosPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .noCollission()
                    .lightLevel(state -> 11)
                    .strength(-1.0F)
                    .sound(SoundType.GLASS)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn((s, r, p, t) -> false)
                    .pushReaction(PushReaction.BLOCK)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ModBlocksItems.registerBlockItem(name, registered);
        return registered;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    // Separate registration for block items
    public static class ModBlocksItems {
        public static final DeferredRegister<Item> ITEMS =
                DeferredRegister.create(ForgeRegistries.ITEMS, until_eternity.MODID);

        private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
            ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }

        public static void register(IEventBus eventBus) {
            ITEMS.register(eventBus);
        }
    }
}
