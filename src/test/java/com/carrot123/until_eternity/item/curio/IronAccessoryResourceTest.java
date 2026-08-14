package com.carrot123.until_eternity.item.curio;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IronAccessoryResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path RESOURCES = ROOT.resolve(
            Path.of("src", "main", "resources"));
    private static final Set<String> ITEMS = Set.of(
            "empowered_ring",
            "advanced_empowered_ring",
            "aetherlight_ring",
            "resonance_armor",
            "greater_arcane_ring");

    @Test
    void registrationsUseDistinctIdsLimitsAndIronAttributes()
            throws IOException {
        String source = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "item", "ModItems.java")));

        for (String item : ITEMS) {
            assertTrue(source.contains("ITEMS.register(\"" + item + "\""));
        }
        assertTrue(source.contains("\"empowered_ring/spell_power\""));
        assertTrue(source.contains("\"advanced_empowered_ring/spell_power\""));
        assertTrue(source.contains("\"aetherlight_ring/max_mana\""));
        assertTrue(source.contains("\"aetherlight_ring/mana_regen\""));
        assertTrue(source.contains(
                "\"resonance_armor/cooldown_reduction\""));
        assertTrue(source.contains("0.10D"));
        assertTrue(source.contains("0.20D"));
        assertTrue(source.contains("0.25D"));
        assertTrue(source.contains("200.0D"));
        assertTrue(source.contains("AttributeRegistry.SPELL_POWER::get"));

        String item = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "item", "curio",
                "IronAttributeCurioItem.java")));
        String base = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "item", "curio",
                "BaseModCurioItem.java")));
        assertTrue(item.contains("countEquippedExcept("));
        assertTrue(item.contains("< maxEquipped"));
        assertFalse(item.contains("findFirstCurio"));
        assertFalse(item.contains("slotContext.identifier()"));
        assertTrue(base.contains("slotContext.cosmetic()"));
        assertFalse(base.contains("slotContext.identifier()"));
    }

    @Test
    void curioTagsAndProductionSlotSizeMatchThePlan() throws IOException {
        assertEquals(Set.of(
                "until_eternity:empowered_ring",
                "until_eternity:advanced_empowered_ring",
                "until_eternity:aetherlight_ring",
                "until_eternity:greater_arcane_ring"),
                values("magic_ring.json"));
        assertTrue(values("accessory.json").contains(
                "until_eternity:resonance_armor"));

        JsonObject slot = readJson(RESOURCES.resolve(Path.of(
                "data", "until_eternity", "curios", "slots",
                "magic_ring.json")));
        assertEquals(2, slot.get("size").getAsInt());
    }

    @Test
    void modelsAndTranslationsExistWithoutPlaceholderTexturesOrRecipes()
            throws IOException {
        Map<String, String> expectedNames = Map.of(
                "empowered_ring", "Empowered Ring",
                "advanced_empowered_ring", "Advanced Empowered Ring",
                "aetherlight_ring", "Aetherlight Ring",
                "resonance_armor", "Resonance Armor",
                "greater_arcane_ring", "Greater Arcane Ring");
        JsonObject english = readJson(RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "lang", "en_us.json")));
        JsonObject chinese = readJson(RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "lang", "zh_cn.json")));

        for (String item : ITEMS) {
            JsonObject model = readJson(RESOURCES.resolve(Path.of(
                    "assets", "until_eternity", "models", "item",
                    item + ".json")));
            assertEquals(
                    "until_eternity:item/" + item,
                    model.getAsJsonObject("textures")
                            .get("layer0").getAsString());
            assertEquals(
                    expectedNames.get(item),
                    english.get("item.until_eternity." + item)
                            .getAsString());
            assertTrue(chinese.has("item.until_eternity." + item));
            assertTrue(Files.isRegularFile(RESOURCES.resolve(Path.of(
                    "assets", "until_eternity", "textures", "item",
                    item + ".png"))));
            assertFalse(Files.exists(RESOURCES.resolve(Path.of(
                    "data", "until_eternity", "recipes",
                    item + ".json"))));
        }
        assertTrue(english.has("enchantment.until_eternity.empowerment"));
        assertTrue(chinese.has("enchantment.until_eternity.empowerment"));
        assertTrue(english.has(
                "enchantment.until_eternity.empowerment.desc"));
        assertTrue(chinese.has(
                "enchantment.until_eternity.empowerment.desc"));
        assertFalse(Files.exists(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "event",
                "EmpowermentTooltipEvents.java"))));
    }

    @Test
    void runtimeEmpowermentCodeDoesNotClampToNaturalMaximum()
            throws IOException {
        String level = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "enchantment",
                "EmpowermentLevel.java")));

        assertFalse(level.contains("Math.min"));
        assertFalse(level.contains("Mth.clamp"));
        assertFalse(level.contains("getMaxLevel()"));
        assertFalse(level.contains("level > 5"));
        assertFalse(level.contains("level == 5"));
    }

    private static Set<String> values(String fileName) throws IOException {
        JsonArray values = readJson(RESOURCES.resolve(Path.of(
                "data", "curios", "tags", "items", fileName)))
                .getAsJsonArray("values");
        Set<String> result = new HashSet<>();
        values.forEach(value -> result.add(value.getAsString()));
        return result;
    }

    private static JsonObject readJson(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path))
                .getAsJsonObject();
    }
}
