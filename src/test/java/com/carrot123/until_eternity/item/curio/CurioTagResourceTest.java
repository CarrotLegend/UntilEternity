package com.carrot123.until_eternity.item.curio;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CurioTagResourceTest {
    private static final Path TAG_DIRECTORY = Path.of(
            "src", "main", "resources", "data", "curios", "tags", "items");

    private static final Set<String> TARGET_ITEMS = Set.of(
            "until_eternity:elemental_gauntlet",
            "until_eternity:reaper_tooth_necklace",
            "until_eternity:sand_shark_tooth_necklace",
            "until_eternity:regenerator",
            "until_eternity:guttering_candle",
            "until_eternity:empowered_shield",
            "until_eternity:cosmic_aegis",
            "until_eternity:proof_of_spurner",
            "until_eternity:dying_fury"
    );

    @Test
    void accessoryAndCharmTagsHaveTheExpectedMembers() throws IOException {
        assertEquals(Set.of(
                "until_eternity:elemental_gauntlet",
                "until_eternity:reaper_tooth_necklace",
                "until_eternity:sand_shark_tooth_necklace",
                "until_eternity:empowered_shield",
                "until_eternity:cosmic_aegis",
                "until_eternity:resonance_armor"
        ), values("accessory.json"));

        assertEquals(Set.of(
                "until_eternity:divine_soul_lamp",
                "until_eternity:regenerator",
                "until_eternity:guttering_candle",
                "until_eternity:proof_of_spurner",
                "until_eternity:dying_fury"
        ), values("charm.json"));
    }

    @Test
    void targetItemsDoNotRemainInOtherCurioTags() throws IOException {
        try (var files = Files.list(TAG_DIRECTORY)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                String name = file.getFileName().toString();
                if ("accessory.json".equals(name) || "charm.json".equals(name)) {
                    continue;
                }
                Set<String> values = values(name);
                for (String item : TARGET_ITEMS) {
                    assertFalse(values.contains(item), item + " remains in " + name);
                }
            }
        }
    }

    private static Set<String> values(String fileName) throws IOException {
        JsonObject root = JsonParser.parseString(
                Files.readString(TAG_DIRECTORY.resolve(fileName))).getAsJsonObject();
        JsonArray values = root.getAsJsonArray("values");
        Set<String> result = new HashSet<>();
        values.forEach(value -> result.add(value.getAsString()));
        return result;
    }
}
