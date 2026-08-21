package com.carrot123.until_eternity.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplicaGelResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of(
            "src", "main", "resources"));

    @Test
    void itemUsesOnlyStackSensitiveCraftingRemainderHooks()
            throws IOException {
        String source = Files.readString(
                JAVA.resolve("item/ReplicaGelItem.java"));

        assertTrue(source.contains("MAX_DURABILITY = 1024"));
        assertTrue(source.contains(".stacksTo(1)"));
        assertTrue(source.contains(".durability(MAX_DURABILITY)"));
        assertTrue(source.contains(
                "hasCraftingRemainingItem(ItemStack stack)"));
        assertTrue(source.contains(
                "getCraftingRemainingItem(ItemStack stack)"));
        assertTrue(source.contains("ItemStack result = stack.copy()"));
        assertTrue(source.contains("result.setCount(1)"));
        assertTrue(source.contains("result.setDamageValue(nextDamage)"));
        assertFalse(source.contains("hurtAndBreak"));
        assertFalse(source.contains("inventoryTick"));
        assertFalse(source.contains("use("));
    }

    @Test
    void registrationCreativeTabLanguagesAndModelAreComplete()
            throws IOException {
        String items = Files.readString(JAVA.resolve("item/ModItems.java"));
        assertTrue(items.contains(
                "ITEMS.register(\"replica_gel\", ReplicaGelItem::new)"));

        String tab = Files.readString(
                JAVA.resolve("item/ModCreativeModeTabs.java"));
        int elementalCore = tab.indexOf(
                "output.accept(ModItems.ELEMENTAL_CORE.get())");
        int replicaGel = tab.indexOf(
                "output.accept(ModItems.REPLICA_GEL.get())");
        assertTrue(elementalCore >= 0);
        assertTrue(replicaGel > elementalCore);

        JsonObject en = json("assets/until_eternity/lang/en_us.json");
        JsonObject zh = json("assets/until_eternity/lang/zh_cn.json");
        assertEquals("Replica Gel", en.get(
                "item.until_eternity.replica_gel").getAsString());
        assertEquals("复刻凝胶", zh.get(
                "item.until_eternity.replica_gel").getAsString());

        JsonObject model = json(
                "assets/until_eternity/models/item/replica_gel.json");
        assertEquals("minecraft:item/generated",
                model.get("parent").getAsString());
        assertEquals("until_eternity:item/replica_gel",
                model.getAsJsonObject("textures")
                        .get("layer0").getAsString());
    }

    @Test
    void featureDoesNotAddRecipes() throws IOException {
        Path recipes = RESOURCES.resolve(
                "data/until_eternity/recipes");
        if (Files.isDirectory(recipes)) {
            try (var paths = Files.walk(recipes)) {
                assertFalse(paths.filter(Files::isRegularFile)
                        .anyMatch(path -> {
                            try {
                                return Files.readString(path)
                                        .contains("replica_gel");
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        }));
            }
        }
    }

    private static JsonObject json(String relative) throws IOException {
        return JsonParser.parseString(Files.readString(
                RESOURCES.resolve(relative))).getAsJsonObject();
    }
}
