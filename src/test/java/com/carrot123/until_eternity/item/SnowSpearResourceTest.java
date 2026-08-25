package com.carrot123.until_eternity.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowSpearResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path MAIN = ROOT.resolve(Path.of("src", "main"));

    @Test
    void playerAttackReplacesTheOneVanillaHurtSourceWithoutReplayingDamage()
            throws IOException {
        String item = source("item/SnowSpear.java");
        String mixin = source("mixin/SnowSpearPlayerAttackMixin.java");
        String registration = source("item/ModItems.java");

        assertTrue(item.contains("BASE_ATTACK_DAMAGE = 500.0F"));
        assertTrue(item.contains("new DamageSource(frostType, attacker, attacker)"));
        assertFalse(item.contains("target.hurt("));
        assertFalse(item.contains("frozenDamage"));
        assertFalse(item.contains("Optional<"));
        assertEquals(1, occurrences(item, "super.hurtEnemy("));

        assertTrue(mixin.contains("Player.class"));
        assertTrue(mixin.contains("Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"));
        assertTrue(mixin.contains("player.getMainHandItem().is(ModItems.SNOW_SPEAR.get())"));
        assertTrue(mixin.contains("SnowSpear.frostDamageSource(player.level(), player)"));
        assertEquals(1, occurrences(mixin, "original.call("));
        assertFalse(mixin.contains("ThreadLocal"));
        assertFalse(mixin.contains("target.hurt("));

        assertTrue(registration.contains(
                "new SnowSpear(Tiers.NETHERITE, -2.5F"));
        assertFalse(registration.contains("400.0F"));
    }

    @Test
    void frostDamageTypeUsesFreezingAndAetherColdTagsWithoutAnyBypass()
            throws IOException {
        JsonObject freezing = json(Path.of(
                "data", "minecraft", "tags", "damage_type", "is_freezing.json"));
        JsonObject aetherCold = json(Path.of(
                "data", "aether", "tags", "damage_type", "is_cold.json"));
        JsonObject damageType = json(Path.of(
                "data", "until_eternity", "damage_type", "frost_bitten.json"));

        assertFalse(freezing.get("replace").getAsBoolean());
        assertEquals(JsonParser.parseString("[\"until_eternity:frost_bitten\"]"),
                freezing.getAsJsonArray("values"));
        assertFalse(aetherCold.get("replace").getAsBoolean());
        assertEquals(JsonParser.parseString("[\"until_eternity:frost_bitten\"]"),
                aetherCold.getAsJsonArray("values"));
        assertEquals("freezing", damageType.get("effects").getAsString());

        Path damageTags = MAIN.resolve(Path.of("resources", "data", "minecraft",
                "tags", "damage_type"));
        try (var paths = Files.walk(damageTags)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    String resource = Files.readString(path);
                    if (path.getFileName().toString().contains("bypass")) {
                        assertFalse(resource.contains("until_eternity:frost_bitten"),
                                path.toString());
                    }
                } catch (IOException exception) {
                    throw new AssertionError(exception);
                }
            });
        }
    }

    @Test
    void mixinAndTooltipResourcesAreRegistered() throws IOException {
        JsonObject config = JsonParser.parseString(Files.readString(MAIN.resolve(Path.of(
                "resources", "until_eternity.mixins.json")))).getAsJsonObject();
        JsonArray mixins = config.getAsJsonArray("mixins");
        assertEquals(1, mixins.asList().stream()
                .filter(value -> value.getAsString().equals("SnowSpearPlayerAttackMixin"))
                .count());

        String english = Files.readString(MAIN.resolve(Path.of(
                "resources", "assets", "until_eternity", "lang", "en_us.json")));
        String chinese = Files.readString(MAIN.resolve(Path.of(
                "resources", "assets", "until_eternity", "lang", "zh_cn.json")));
        assertTrue(english.contains("item.until_eternity.snow_spear.desc"));
        assertTrue(chinese.contains("item.until_eternity.snow_spear.desc"));
    }

    private static String source(String path) throws IOException {
        return Files.readString(MAIN.resolve(Path.of(
                "java", "com", "carrot123", "until_eternity", path)));
    }

    private static JsonObject json(Path relative) throws IOException {
        return JsonParser.parseString(Files.readString(
                MAIN.resolve("resources").resolve(relative))).getAsJsonObject();
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
