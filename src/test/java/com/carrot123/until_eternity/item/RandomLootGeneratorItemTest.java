package com.carrot123.until_eternity.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomLootGeneratorItemTest {
    private static final List<String> EXPECTED_LOOT_TABLES = List.of(
            "minecraft:chests/abandoned_mineshaft",
            "minecraft:chests/village/village_armorer",
            "minecraft:chests/village/village_butcher",
            "minecraft:chests/village/village_cartographer",
            "minecraft:chests/village/village_desert_house",
            "minecraft:chests/village/village_fisher",
            "minecraft:chests/village/village_fletcher",
            "minecraft:chests/village/village_mason",
            "minecraft:chests/village/village_plains_house",
            "minecraft:chests/village/village_savanna_house",
            "minecraft:chests/village/village_shepherd",
            "minecraft:chests/village/village_snowy_house",
            "minecraft:chests/village/village_taiga_house",
            "minecraft:chests/village/village_tannery",
            "minecraft:chests/village/village_temple",
            "minecraft:chests/village/village_toolsmith",
            "minecraft:chests/village/village_weaponsmith",
            "minecraft:chests/ancient_city",
            "minecraft:chests/simple_dungeon",
            "minecraft:chests/stronghold_corridor",
            "minecraft:chests/stronghold_crossing",
            "minecraft:chests/stronghold_library",
            "minecraft:chests/ruined_portal",
            "minecraft:chests/shipwreck_map",
            "minecraft:chests/shipwreck_supply",
            "minecraft:chests/shipwreck_treasure"
    );

    @Test
    void poolHasAllTwentySixUniqueVanillaTables() {
        List<ResourceLocation> actual = RandomLootGeneratorData.LOOT_TABLE_IDS;
        assertEquals(EXPECTED_LOOT_TABLES, actual.stream().map(ResourceLocation::toString).toList());
        assertEquals(26, new HashSet<>(actual).size());
        for (ResourceLocation id : actual) {
            String resource = "data/minecraft/loot_tables/"
                    + id.getPath() + ".json";
            try (InputStream stream = getResource(resource)) {
                assertNotNull(stream, id + " must exist in the Minecraft 1.20.1 resources");
            } catch (Exception exception) {
                fail(exception);
            }
        }
    }

    @Test
    void storedLootTableIdsAreValidatedWithoutChangingThem() {
        assertEquals(new ResourceLocation("minecraft", "chests/simple_dungeon"),
                RandomLootGeneratorData.parseLootTableId("minecraft:chests/simple_dungeon"));
        assertNull(RandomLootGeneratorData.parseLootTableId("not a valid id"));
    }

    @Test
    void assignmentOnlyRunsWhenTheNbtKeyIsAbsent() {
        CompoundTag unassigned = new CompoundTag();
        assertTrue(RandomLootGeneratorData.assignLootTableIfMissing(unassigned, RandomSource.create(42L)));
        String assigned = unassigned.getString(RandomLootGeneratorData.TAG_LOOT_TABLE);
        assertTrue(EXPECTED_LOOT_TABLES.contains(assigned));
        assertFalse(RandomLootGeneratorData.assignLootTableIfMissing(unassigned, RandomSource.create(7L)));
        assertEquals(assigned, unassigned.getString(RandomLootGeneratorData.TAG_LOOT_TABLE));

        CompoundTag supplied = new CompoundTag();
        supplied.putString(RandomLootGeneratorData.TAG_LOOT_TABLE, "minecraft:chests/ancient_city");
        assertFalse(RandomLootGeneratorData.assignLootTableIfMissing(supplied, RandomSource.create(1L)));
        assertEquals("minecraft:chests/ancient_city", supplied.getString(RandomLootGeneratorData.TAG_LOOT_TABLE));

        CompoundTag corrupt = new CompoundTag();
        corrupt.putInt(RandomLootGeneratorData.TAG_LOOT_TABLE, 9);
        assertFalse(RandomLootGeneratorData.assignLootTableIfMissing(corrupt, RandomSource.create(1L)));
        assertEquals(9, corrupt.getInt(RandomLootGeneratorData.TAG_LOOT_TABLE));
    }

    @Test
    void modelRecipeLanguagesAndRenamedTextureAreValid() throws Exception {
        JsonObject model = json("assets/until_eternity/models/item/random_loot_generator.json");
        assertEquals("minecraft:item/generated", model.get("parent").getAsString());
        assertEquals("until_eternity:item/random_loot_generator",
                model.getAsJsonObject("textures").get("layer0").getAsString());

        JsonObject recipe = json("data/until_eternity/recipes/random_loot_generator.json");
        assertEquals("GGG", recipe.getAsJsonArray("pattern").get(0).getAsString());
        assertEquals("GEG", recipe.getAsJsonArray("pattern").get(1).getAsString());
        assertEquals("GCG", recipe.getAsJsonArray("pattern").get(2).getAsString());
        assertEquals("enigmaticaddons:earth_heart_fragment",
                recipe.getAsJsonObject("key").getAsJsonObject("E").get("item").getAsString());
        assertEquals("forge:chests/wooden",
                recipe.getAsJsonObject("key").getAsJsonObject("C").get("tag").getAsString());
        assertEquals("until_eternity:random_loot_generator",
                recipe.getAsJsonObject("result").get("item").getAsString());

        JsonObject chinese = json("assets/until_eternity/lang/zh_cn.json");
        JsonObject english = json("assets/until_eternity/lang/en_us.json");
        assertEquals("随机战利品生成器", chinese.get("item.until_eternity.random_loot_generator").getAsString());
        assertEquals("Right-click to open a random loot table",
                english.get("tooltip.until_eternity.random_loot_generator").getAsString());

        assertNull(getResource("assets/until_eternity/textures/item/barrel_top.png"));
        try (InputStream texture = getResource("assets/until_eternity/textures/item/random_loot_generator.png")) {
            assertNotNull(texture);
            String hash = Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(texture.readAllBytes())
            );
            assertEquals("9gnqZueJtTVD+L/02mWuu+GIHJoihsrf1vG6iwW6C7A=", hash);
        }
    }

    private static JsonObject json(String path) throws Exception {
        try (InputStream stream = getResource(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static InputStream getResource(String path) {
        return RandomLootGeneratorItemTest.class.getClassLoader().getResourceAsStream(path);
    }
}
