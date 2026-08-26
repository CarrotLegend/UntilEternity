package com.carrot123.until_eternity.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobContainerResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of(
            "src", "main", "resources"));

    private static final Set<String> VANILLA_WHITELIST = Set.of(
            "minecraft:allay", "minecraft:axolotl", "minecraft:bat",
            "minecraft:bee", "minecraft:blaze", "minecraft:camel",
            "minecraft:cat", "minecraft:cave_spider", "minecraft:chicken",
            "minecraft:cod", "minecraft:cow", "minecraft:creeper",
            "minecraft:dolphin", "minecraft:donkey", "minecraft:drowned",
            "minecraft:elder_guardian", "minecraft:enderman",
            "minecraft:endermite", "minecraft:evoker", "minecraft:fox",
            "minecraft:frog", "minecraft:ghast", "minecraft:giant",
            "minecraft:glow_squid", "minecraft:goat", "minecraft:guardian",
            "minecraft:hoglin", "minecraft:horse", "minecraft:husk",
            "minecraft:illusioner", "minecraft:iron_golem", "minecraft:llama",
            "minecraft:magma_cube", "minecraft:mooshroom", "minecraft:mule",
            "minecraft:ocelot", "minecraft:panda", "minecraft:parrot",
            "minecraft:phantom", "minecraft:pig", "minecraft:piglin",
            "minecraft:piglin_brute", "minecraft:pillager",
            "minecraft:polar_bear", "minecraft:pufferfish", "minecraft:rabbit",
            "minecraft:ravager", "minecraft:salmon", "minecraft:sheep",
            "minecraft:shulker", "minecraft:silverfish", "minecraft:skeleton",
            "minecraft:skeleton_horse", "minecraft:slime", "minecraft:sniffer",
            "minecraft:snow_golem", "minecraft:spider", "minecraft:squid",
            "minecraft:stray", "minecraft:strider", "minecraft:tadpole",
            "minecraft:trader_llama", "minecraft:tropical_fish",
            "minecraft:turtle", "minecraft:vex", "minecraft:villager",
            "minecraft:vindicator", "minecraft:wandering_trader",
            "minecraft:witch", "minecraft:wither_skeleton", "minecraft:wolf",
            "minecraft:zoglin", "minecraft:zombie", "minecraft:zombie_horse",
            "minecraft:zombie_villager", "minecraft:zombified_piglin");

    @Test
    void registrationCreativeTabAndEndTableRarityUseExistingSystems()
            throws IOException {
        String items = source("item/ModItems.java");
        String tabs = source("item/ModCreativeModeTabs.java");

        assertTrue(items.contains(
                "ITEMS.register(\"mob_container\", MobContainerItem::new)"));
        assertTrue(tabs.contains(
                "output.accept(ModItems.MOB_CONTAINER.get())"));
        assertTrue(items.contains(
                "new EndCraftingTableBlockItem(ModBlocks.END_CRAFTING_TABLE.get(),"));
        assertTrue(items.contains(
                "new Item.Properties().rarity(Rarity.RARE)"));
        assertEquals(1, occurrences(items,
                "new EndCraftingTableBlockItem("));
    }

    @Test
    void itemUsesOneTransactionalFullNbtCaptureAndReleasePath()
            throws IOException {
        String item = source("item/MobContainerItem.java");

        assertTrue(item.contains(
                "TAG_STORED_ENTITY = \"StoredEntity\""));
        assertTrue(item.contains(".stacksTo(1)"));
        assertTrue(item.contains(".rarity(Rarity.UNCOMMON)"));
        assertEquals(1, occurrences(item, "target.save(storedEntity)"));
        assertEquals(1, occurrences(item, "target.discard()"));
        assertTrue(item.indexOf("target.save(storedEntity)")
                < item.indexOf("target.discard()"));
        assertTrue(item.contains("stack.getOrCreateTag().put("));
        assertTrue(item.indexOf("stack.getOrCreateTag().put(")
                < item.indexOf("target.discard()"));
        assertTrue(item.contains("EntityType.loadEntityRecursive("));
        assertTrue(item.contains(
                "serverLevel.tryAddFreshEntityWithPassengers(restored)"));
        assertTrue(item.indexOf("tryAddFreshEntityWithPassengers(restored)")
                < item.indexOf("stack.removeTagKey(TAG_STORED_ENTITY)"));
        assertFalse(item.contains("EntityType.create("));
        assertFalse(item.contains("setUUID("));
        assertTrue(item.contains("storedEntity.copy()"));
        assertTrue(item.contains("setDeltaMovement(Vec3.ZERO)"));
    }

    @Test
    void hardBansPrecedeTheExtensibleWhitelist() throws IOException {
        String item = source("item/MobContainerItem.java");
        String tags = source("registry/ModTags.java");

        int player = item.indexOf("entity instanceof Player");
        int warden = item.indexOf("entity.getType() == EntityType.WARDEN");
        int boss = item.indexOf("entity.getType().is(ModTags.EntityTypes.BOSS)");
        int whitelist = item.indexOf("!entity.getType().is(");
        assertTrue(player >= 0 && player < warden);
        assertTrue(warden < boss);
        assertTrue(boss < whitelist);
        assertTrue(item.contains("!entity.isPassenger()"));
        assertTrue(item.contains("!entity.isVehicle()"));

        assertTrue(tags.contains("create(\"c\", \"boss\")"));
        assertTrue(tags.contains(
                "create(until_eternity.MODID, \"mob_container_whitelist\")"));
        assertTrue(tags.contains("Registries.ENTITY_TYPE"));
    }

    @Test
    void defaultWhitelistIsTheAuditedSetOfSeventySixMobs()
            throws IOException {
        JsonObject tag = json(
                "data/until_eternity/tags/entity_types/"
                        + "mob_container_whitelist.json");
        assertFalse(tag.get("replace").getAsBoolean());
        JsonArray values = tag.getAsJsonArray("values");
        Set<String> actual = values.asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());
        assertEquals(76, values.size());
        assertEquals(76, actual.size());
        assertEquals(VANILLA_WHITELIST, actual);
        assertFalse(actual.contains("minecraft:player"));
        assertFalse(actual.contains("minecraft:warden"));
        assertFalse(actual.contains("minecraft:ender_dragon"));
        assertFalse(actual.contains("minecraft:wither"));
        assertFalse(actual.contains("minecraft:armor_stand"));
    }

    @Test
    void recipeModelTextureAndTranslationsAreComplete() throws IOException {
        JsonObject recipe = json(
                "data/until_eternity/recipes/mob_container.json");
        assertEquals("minecraft:crafting_shaped",
                recipe.get("type").getAsString());
        assertEquals(JsonParser.parseString("[\"III\",\"ICI\",\"III\"]"),
                recipe.getAsJsonArray("pattern"));
        assertEquals("minecraft:iron_bars",
                recipe.getAsJsonObject("key").getAsJsonObject("I")
                        .get("item").getAsString());
        assertEquals("forge:chests",
                recipe.getAsJsonObject("key").getAsJsonObject("C")
                        .get("tag").getAsString());
        assertEquals("until_eternity:mob_container",
                recipe.getAsJsonObject("result").get("item").getAsString());
        assertEquals(1,
                recipe.getAsJsonObject("result").get("count").getAsInt());

        JsonObject model = json(
                "assets/until_eternity/models/item/mob_container.json");
        assertEquals("minecraft:item/generated",
                model.get("parent").getAsString());
        assertEquals("until_eternity:item/mob_container",
                model.getAsJsonObject("textures").get("layer0").getAsString());

        Path texture = RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "textures", "item",
                "mob_container.png"));
        BufferedImage image = ImageIO.read(texture.toFile());
        assertNotNull(image);
        assertEquals(16, image.getWidth());
        assertEquals(16, image.getHeight());
        assertTrue(image.getColorModel().hasAlpha());
        assertFalse(Files.exists(RESOURCES.resolve("assets/model.png")));

        JsonObject english = json(
                "assets/until_eternity/lang/en_us.json");
        JsonObject chinese = json(
                "assets/until_eternity/lang/zh_cn.json");
        for (JsonObject language : Set.of(english, chinese)) {
            assertTrue(language.has("item.until_eternity.mob_container"));
            assertTrue(language.has(
                    "tooltip.until_eternity.mob_container.usage"));
            assertTrue(language.has(
                    "tooltip.until_eternity.mob_container.current"));
            assertTrue(language.has(
                    "tooltip.until_eternity.mob_container.empty"));
            assertTrue(language.has(
                    "tooltip.until_eternity.mob_container.unknown"));
        }
        assertEquals("生物收容器",
                chinese.get("item.until_eternity.mob_container").getAsString());
        assertEquals("Mob Container",
                english.get("item.until_eternity.mob_container").getAsString());
    }

    @Test
    void tooltipUsesStoredCustomNameThenRegistryWithoutSpawning()
            throws IOException {
        String item = source("item/MobContainerItem.java");
        assertEquals(2, occurrences(item, "tooltip.add("));
        assertTrue(item.contains("Component.Serializer.fromJson("));
        assertTrue(item.contains(
                "ForgeRegistries.ENTITY_TYPES.getValue(entityId)"));
        assertTrue(item.contains("entityType.getDescription()"));
        assertFalse(item.contains("create(level"));
    }

    private static String source(String relative) throws IOException {
        return Files.readString(JAVA.resolve(relative));
    }

    private static JsonObject json(String relative) throws IOException {
        return JsonParser.parseString(Files.readString(
                RESOURCES.resolve(relative))).getAsJsonObject();
    }

    private static int occurrences(String source, String target) {
        return (source.length() - source.replace(target, "").length())
                / target.length();
    }
}
