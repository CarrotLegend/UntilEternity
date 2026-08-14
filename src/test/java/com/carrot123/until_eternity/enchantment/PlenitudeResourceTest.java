package com.carrot123.until_eternity.enchantment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlenitudeResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path MAIN_JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of(
            "src", "main", "resources"));

    @Test
    void registersTagBoundNaturalLevelThreeEnchantment() throws IOException {
        String registry = Files.readString(MAIN_JAVA.resolve(Path.of(
                "enchantment", "ModEnchantments.java")));
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "enchantment", "PlenitudeEnchantment.java")));

        assertTrue(registry.contains(
                "ENCHANTMENTS.register(\"plenitude\", "
                        + "PlenitudeEnchantment::new)"));
        assertTrue(source.contains("return 3;"));
        assertTrue(source.contains("IronSpellbookTags.STAFFS"));
        assertTrue(source.contains("Items.BOOK"));
        assertTrue(source.contains("Items.ENCHANTED_BOOK"));
        assertTrue(source.contains("return false;"));
        assertFalse(source.contains("StaffItem"));
    }

    @Test
    void sharedActualLevelReaderSupportsBothNbtListsWithoutMutation()
            throws IOException {
        String shared = Files.readString(MAIN_JAVA.resolve(Path.of(
                "enchantment", "ActualEnchantmentLevel.java")));
        String empowerment = Files.readString(MAIN_JAVA.resolve(Path.of(
                "enchantment", "EmpowermentLevel.java")));

        assertTrue(shared.contains("\"Enchantments\""));
        assertTrue(shared.contains("\"StoredEnchantments\""));
        assertTrue(shared.contains("Tag.TAG_ANY_NUMERIC"));
        assertTrue(shared.contains(
                "EnchantmentHelper.getItemEnchantmentLevel"));
        assertFalse(shared.contains(".put("));
        assertTrue(empowerment.contains("ActualEnchantmentLevel.read("));
    }

    @Test
    void lootModifierAndDefaultsArePresent() throws IOException {
        JsonObject list = JsonParser.parseString(Files.readString(
                RESOURCES.resolve(Path.of(
                        "data", "forge", "loot_modifiers",
                        "global_loot_modifiers.json")))).getAsJsonObject();
        assertFalse(list.get("replace").getAsBoolean());
        assertEquals(
                "until_eternity:plenitude_loot",
                list.getAsJsonArray("entries").get(0).getAsString());

        JsonObject modifier = JsonParser.parseString(Files.readString(
                RESOURCES.resolve(Path.of(
                        "data", "until_eternity", "loot_modifiers",
                        "plenitude_loot.json")))).getAsJsonObject();
        assertEquals(
                "until_eternity:plenitude_loot",
                modifier.get("type").getAsString());

        String config = Files.readString(MAIN_JAVA.resolve("Config.java"));
        for (String table : Set.of(
                "minecraft:chests/ancient_city",
                "minecraft:chests/end_city_treasure",
                "minecraft:chests/stronghold_library",
                "minecraft:chests/simple_dungeon",
                "minecraft:chests/woodland_mansion")) {
            assertTrue(config.contains(table));
        }
        assertTrue(config.contains(
                "defineInRange(\"plenitudeLootChance\", 0.08D"));
        assertTrue(config.contains(
                "\"plenitudeLootOutputItem\", "
                        + "\"minecraft:enchanted_book\""));
    }

    @Test
    void translationsDescribeUncappedRuntimeEffect() throws IOException {
        for (String language : Set.of("en_us.json", "zh_cn.json")) {
            JsonObject translations = JsonParser.parseString(
                    Files.readString(RESOURCES.resolve(Path.of(
                            "assets", "until_eternity", "lang", language))))
                    .getAsJsonObject();
            assertTrue(translations.has(
                    "enchantment.until_eternity.plenitude"));
            assertTrue(translations.has(
                    "enchantment.until_eternity.plenitude.desc"));
        }
    }

    @Test
    void runtimeManaCalculationDoesNotClampToNaturalMaximum()
            throws IOException {
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "compat", "ironsspellbooks",
                "PlenitudeManaCost.java")));
        assertFalse(source.contains("Math.min(level, 3)"));
        assertFalse(source.contains("Mth.clamp"));
        assertTrue(source.contains("Math.max(1, reducedCost)"));
        assertTrue(source.contains("ActualEnchantmentLevel.read("));
    }
}
