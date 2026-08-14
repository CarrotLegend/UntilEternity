package com.carrot123.until_eternity;

import org.slf4j.Logger;

import com.carrot123.until_eternity.block.ModBlocks;
import com.carrot123.until_eternity.enchantment.ModEnchantments;
import com.carrot123.until_eternity.event.CurioEventHandler;
import com.carrot123.until_eternity.event.EnchantmentEventHandler;
import com.carrot123.until_eternity.item.ModCreativeModeTabs;
import com.carrot123.until_eternity.item.ModItems;
import com.carrot123.until_eternity.loot.ModLootModifiers;
import com.carrot123.until_eternity.particle.ModParticles;
import com.carrot123.until_eternity.recipe.ModRecipeSerializers;
import com.carrot123.until_eternity.registry.ModMobEffects;
import com.carrot123.until_eternity.registry.ModPotions;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(until_eternity.MODID)
public class until_eternity
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "until_eternity";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public until_eternity(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEnchantments.register(modEventBus);
        ModParticles.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModPotions.register(modEventBus);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new CurioEventHandler());
        MinecraftForge.EVENT_BUS.register(new EnchantmentEventHandler());

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            // Chaos portal translucent rendering
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHAOS_PORTAL.get(), RenderType.translucent());
        }
    }
}
