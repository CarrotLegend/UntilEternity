package com.carrot123.until_eternity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = until_eternity.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PLENITUDE_LOOT_TABLES =
            BUILDER.comment("Chest loot tables that may generate Plenitude.")
                    .defineListAllowEmpty(
                            "plenitudeLootTables",
                            List.of(
                                    "minecraft:chests/ancient_city",
                                    "minecraft:chests/end_city_treasure",
                                    "minecraft:chests/stronghold_library",
                                    "minecraft:chests/simple_dungeon",
                                    "minecraft:chests/woodland_mansion"),
                            Config::validateChestLootTable);
    private static final ForgeConfigSpec.DoubleValue PLENITUDE_LOOT_CHANCE =
            BUILDER.comment("Chance for an allowed chest to receive one Plenitude item.")
                    .defineInRange("plenitudeLootChance", 0.08D, 0.0D, 1.0D);
    private static final ForgeConfigSpec.IntValue PLENITUDE_LOOT_MIN_LEVEL =
            BUILDER.comment("Minimum naturally generated Plenitude level.")
                    .defineInRange("plenitudeLootMinLevel", 1, 1, 3);
    private static final ForgeConfigSpec.IntValue PLENITUDE_LOOT_MAX_LEVEL =
            BUILDER.comment("Maximum naturally generated Plenitude level.")
                    .defineInRange("plenitudeLootMaxLevel", 3, 1, 3);
    private static final ForgeConfigSpec.ConfigValue<String> PLENITUDE_LOOT_OUTPUT_ITEM =
            BUILDER.comment("Enchanted book or irons_spellbooks:staff-tagged item to generate.")
                    .define("plenitudeLootOutputItem", "minecraft:enchanted_book",
                            Config::validateResourceLocation);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;
    public static Set<ResourceLocation> plenitudeLootTables;
    public static double plenitudeLootChance;
    public static int plenitudeLootMinLevel;
    public static int plenitudeLootMaxLevel;
    public static ResourceLocation plenitudeLootOutputItem;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(ResourceLocation.tryParse(itemName));
    }

    private static boolean validateResourceLocation(final Object obj) {
        return obj instanceof String value
                && ResourceLocation.tryParse(value) != null;
    }

    private static boolean validateChestLootTable(final Object obj) {
        if (!(obj instanceof String value)) {
            return false;
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        return id != null && id.getPath().startsWith("chests/");
    }

    @SubscribeEvent
    @SuppressWarnings("null")
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(itemName)))
                .filter(item -> item != null)
                .collect(Collectors.toSet());

        plenitudeLootTables = PLENITUDE_LOOT_TABLES.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(id -> id != null && id.getPath().startsWith("chests/"))
                .collect(Collectors.toUnmodifiableSet());
        plenitudeLootChance = PLENITUDE_LOOT_CHANCE.get();
        plenitudeLootMinLevel = Math.min(
                PLENITUDE_LOOT_MIN_LEVEL.get(),
                PLENITUDE_LOOT_MAX_LEVEL.get());
        plenitudeLootMaxLevel = Math.max(
                PLENITUDE_LOOT_MIN_LEVEL.get(),
                PLENITUDE_LOOT_MAX_LEVEL.get());
        plenitudeLootOutputItem = ResourceLocation.tryParse(
                PLENITUDE_LOOT_OUTPUT_ITEM.get());
    }
}
