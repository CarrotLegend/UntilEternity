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
        String events = source("event/MobContainerInteractionHandler.java");

        assertTrue(item.contains(
                "TAG_STORED_ENTITY = \"StoredEntity\""));
        assertTrue(item.contains(".stacksTo(1)"));
        assertTrue(item.contains(".rarity(Rarity.UNCOMMON)"));
        assertEquals(1, occurrences(item, "target.save(storedEntity)"));
        assertEquals(1, occurrences(item, "target.discard()"));
        assertTrue(item.indexOf("target.save(storedEntity)")
                < item.indexOf("target.discard()"));
        assertTrue(item.contains("stack.getOrCreateTag().put("));
        assertTrue(item.contains("storedEntity.copy()"));
        assertTrue(item.indexOf("stack.getOrCreateTag().put(")
                < item.indexOf("target.discard()"));
        assertTrue(item.contains("player.getItemInHand(hand)"));
        assertTrue(item.contains("player.setItemInHand(hand, stack)"));
        assertTrue(item.contains("player.getInventory().setChanged()"));
        assertTrue(item.contains("player.inventoryMenu.broadcastChanges()"));
        assertTrue(item.contains("EntityType.loadEntityRecursive("));
        assertTrue(item.contains(
                "serverLevel.tryAddFreshEntityWithPassengers(restored)"));
        assertTrue(item.indexOf("tryAddFreshEntityWithPassengers(restored)")
                < item.indexOf("stack.removeTagKey(TAG_STORED_ENTITY)"));
        assertFalse(item.contains("EntityType.create("));
        assertFalse(item.contains("setUUID("));
        assertTrue(item.contains("setDeltaMovement(Vec3.ZERO)"));
        assertFalse(item.contains("interactLivingEntity("));
        assertFalse(item.contains("InteractionResult useOn("));
        assertEquals(1, occurrences(events, "container.tryCapture("));
        assertEquals(1, occurrences(events, "container.tryRelease("));
    }

    @Test
    void forgeEventsOwnInteractionPriorityAndClientsOnlyCancel()
            throws IOException {
        String events = source("event/MobContainerInteractionHandler.java");
        String item = source("item/MobContainerItem.java");

        assertTrue(events.contains(
                "PlayerInteractEvent.EntityInteractSpecific event"));
        assertTrue(events.contains(
                "PlayerInteractEvent.EntityInteract event"));
        assertTrue(events.contains(
                "PlayerInteractEvent.RightClickBlock event"));
        assertEquals(3, occurrences(events,
                "@SubscribeEvent(priority = EventPriority.HIGHEST)"));
        assertEquals(2, occurrences(events,
                "handleCaptureInteraction(event, event.getTarget())"));
        assertTrue(events.contains("event.setCanceled(true)"));
        assertTrue(events.contains(
                "InteractionResult.sidedSuccess("));
        assertTrue(events.indexOf("cancelInteraction(event, player)")
                < events.indexOf("if (player.level().isClientSide)"));
        assertFalse(events.contains(".save("));
        assertFalse(events.contains(".discard("));
        assertFalse(events.contains("loadEntityRecursive"));
        assertFalse(events.contains("removeTagKey"));
        assertFalse(item.contains("isCreative"));
        assertFalse(item.contains("instabuild"));
        assertFalse(item.contains("SpawnPlacements"));
        assertFalse(item.contains("MobSpawnType"));
        assertFalse(item.contains("Difficulty"));
    }

    @Test
    void releasePlacementUsesEntityDimensionsAndOneSharedCollisionPath()
            throws IOException {
        String item = source("item/MobContainerItem.java");

        assertTrue(item.contains("RELEASE_EPSILON = 0.01D"));
        assertTrue(item.contains(
                "new double[]{0.0D, 0.5D, 1.0D, 1.5D, 2.0D}"));
        assertTrue(item.contains("entity.getBbWidth() / 2.0D"));
        assertTrue(item.contains("entity.getBbHeight()"));
        assertTrue(item.contains("face.getNormal()"));
        assertTrue(item.contains("level.hasChunksAt(min, max)"));
        assertTrue(item.contains(
                "level.getWorldBorder().isWithinBounds(bounds)"));
        assertEquals(1, occurrences(item,
                "level.noCollision(entity, bounds)"));
        assertEquals(1, occurrences(item,
                "serverLevel.tryAddFreshEntityWithPassengers(restored)"));
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
    void whitelistKeepsUniqueEntriesExtensionsAndHardBans()
            throws IOException {
        JsonObject tag = json(
                "data/until_eternity/tags/entity_types/"
                        + "mob_container_whitelist.json");
        assertFalse(tag.get("replace").getAsBoolean());
        JsonArray values = tag.getAsJsonArray("values");
        Set<String> actual = values.asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());
        assertEquals(values.size(), actual.size());
        assertTrue(actual.contains("minecraft:cow"));
        assertTrue(actual.contains("minecraft:villager"));
        assertTrue(actual.contains("minecraft:zombie"));
        assertTrue(actual.stream().anyMatch(id -> !id.startsWith("minecraft:")));
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
