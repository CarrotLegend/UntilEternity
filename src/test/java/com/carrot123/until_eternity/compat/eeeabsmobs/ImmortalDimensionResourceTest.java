package com.carrot123.until_eternity.compat.eeeabsmobs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImmortalDimensionResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path RESOURCES = ROOT.resolve(Path.of(
            "src", "main", "resources"));
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));

    @Test
    void flatDimensionHasExactlyBedrockAndStone() throws IOException {
        JsonObject dimension = readData("dimension", "immortal.json");
        assertEquals("until_eternity:immortal",
                dimension.get("type").getAsString());

        JsonObject generator = dimension.getAsJsonObject("generator");
        assertEquals("minecraft:flat",
                generator.get("type").getAsString());
        JsonObject settings = generator.getAsJsonObject("settings");
        assertEquals("until_eternity:immortal_wasteland",
                settings.get("biome").getAsString());
        assertFalse(settings.get("features").getAsBoolean());
        assertFalse(settings.get("lakes").getAsBoolean());
        assertTrue(settings.getAsJsonArray("structure_overrides").isEmpty());

        JsonArray layers = settings.getAsJsonArray("layers");
        assertEquals(2, layers.size());
        assertLayer(layers.get(0).getAsJsonObject(),
                "minecraft:bedrock", 1);
        assertLayer(layers.get(1).getAsJsonObject(),
                "minecraft:stone", 63);
    }

    @Test
    void dimensionTypeIsOpenSkyAndFixedAtNoon() throws IOException {
        JsonObject type = readData("dimension_type", "immortal.json");
        assertEquals(6000, type.get("fixed_time").getAsInt());
        assertTrue(type.get("has_skylight").getAsBoolean());
        assertFalse(type.get("has_ceiling").getAsBoolean());
        assertEquals(1.0D, type.get("coordinate_scale").getAsDouble());
        assertEquals(0, type.get("min_y").getAsInt());
        assertEquals(256, type.get("height").getAsInt());
        assertEquals(256, type.get("logical_height").getAsInt());
        assertEquals(15,
                type.get("monster_spawn_block_light_limit").getAsInt());
        assertEquals("minecraft:overworld",
                type.get("effects").getAsString());
    }

    @Test
    void wastelandContainsOnlyThreeImmortalMobsAndNoWorldgen()
            throws IOException {
        JsonObject biome = readData(
                Path.of("worldgen", "biome").toString(),
                "immortal_wasteland.json");
        assertFalse(biome.get("has_precipitation").getAsBoolean());
        assertEquals(0, biome.getAsJsonObject("carvers").size());
        for (var step : biome.getAsJsonArray("features")) {
            assertTrue(step.getAsJsonArray().isEmpty());
        }

        JsonObject spawners = biome.getAsJsonObject("spawners");
        JsonArray monsters = spawners.getAsJsonArray("monster");
        assertEquals(3, monsters.size());
        Set<String> types = StreamSupport.stream(
                        monsters.spliterator(), false)
                .map(entry -> entry.getAsJsonObject()
                        .get("type").getAsString())
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "eeeabsmobs:immortal_skeleton",
                "eeeabsmobs:immortal_knight",
                "eeeabsmobs:immortal_executioner"), types);
        assertMob(monsters.get(0).getAsJsonObject(), 30, 2, 4);
        assertMob(monsters.get(1).getAsJsonObject(), 30, 1, 2);
        assertMob(monsters.get(2).getAsJsonObject(), 30, 1, 1);

        for (String category : List.of(
                "creature", "ambient", "axolotls",
                "underground_water_creature", "water_creature",
                "water_ambient", "misc")) {
            assertTrue(spawners.getAsJsonArray(category).isEmpty());
        }
    }

    @Test
    void spawnGuardAndPortalMixinUseExactContracts()
            throws IOException {
        String spawnSource = Files.readString(JAVA.resolve(Path.of(
                "event", "ImmortalSpawnEvents.java")));
        assertTrue(spawnSource.contains("MobSpawnEvent.SpawnPlacementCheck"));
        assertTrue(spawnSource.contains("MobSpawnEvent.FinalizeSpawn"));
        assertTrue(spawnSource.contains(
                "Monster.checkAnyLightMonsterSpawnRules"));
        assertTrue(spawnSource.contains("EventPriority.LOWEST"));
        assertTrue(spawnSource.contains("event.setSpawnCancelled(true)"));

        String mixin = Files.readString(JAVA.resolve(Path.of(
                "mixin", "compat", "eeeabsmobs",
                "BlockErosionPortalMixin.java")));
        assertTrue(mixin.contains("BlockErosionPortal.class"));
        assertTrue(mixin.contains(
                "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V"));
        assertTrue(mixin.contains("require = 1"));
        assertTrue(mixin.contains("remap = true"));
        assertTrue(mixin.contains("callbackInfo.cancel()"));

        String mixinConfig = Files.readString(RESOURCES.resolve(
                "until_eternity.mixins.json"));
        assertTrue(mixinConfig.contains(
                "compat.eeeabsmobs.BlockErosionPortalMixin"));
    }

    @Test
    void portalReusesEeeabPoiAndPortalConstruction()
            throws IOException {
        String teleporter = Files.readString(JAVA.resolve(Path.of(
                "compat", "eeeabsmobs",
                "ImmortalPortalTeleporter.java")));
        assertTrue(teleporter.contains("VoidCrackTeleporter"));
        assertTrue(teleporter.contains(
                "findPortalAround(\n                        targetPos, false, border)"));
        assertTrue(teleporter.contains("createPortal("));
        assertTrue(teleporter.contains(
                "CuboidPortalShape.getRelativePosition"));
        assertTrue(teleporter.contains(
                "CuboidPortalShape.createPortalInfo"));
        assertTrue(teleporter.contains("repositionEntity.apply(false)"));
        assertFalse(teleporter.contains("extends VoidCrackTeleporter"));
    }

    @Test
    void translationsExistInBothLanguages() throws IOException {
        for (String language : List.of("en_us.json", "zh_cn.json")) {
            JsonObject translations = JsonParser.parseString(
                    Files.readString(RESOURCES.resolve(Path.of(
                            "assets", "until_eternity", "lang",
                            language)))).getAsJsonObject();
            assertTrue(translations.has(
                    "dimension.until_eternity.immortal"));
            assertTrue(translations.has(
                    "biome.until_eternity.immortal_wasteland"));
        }
    }

    private static JsonObject readData(String directory, String file)
            throws IOException {
        return JsonParser.parseString(Files.readString(RESOURCES.resolve(
                Path.of("data", "until_eternity", directory, file))))
                .getAsJsonObject();
    }

    private static void assertLayer(JsonObject layer,
                                    String block,
                                    int height) {
        assertEquals(block, layer.get("block").getAsString());
        assertEquals(height, layer.get("height").getAsInt());
    }

    private static void assertMob(JsonObject mob,
                                  int weight,
                                  int min,
                                  int max) {
        assertEquals(weight, mob.get("weight").getAsInt());
        assertEquals(min, mob.get("minCount").getAsInt());
        assertEquals(max, mob.get("maxCount").getAsInt());
    }
}
