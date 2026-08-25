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
    void frostDamageUsesTheVanillaFreezeTypeWithoutObsoleteCustomResources()
            throws IOException {
        assertTrue(source("item/SnowSpear.java").contains("DamageTypes.FREEZE"));
        assertFalse(Files.exists(MAIN.resolve(Path.of("resources", "data", "until_eternity",
                "damage_type", "frost_bitten.json"))));
        try (var paths = Files.walk(MAIN.resolve("resources"))) {
            assertFalse(paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .anyMatch(path -> {
                try {
                    return Files.readString(path).contains("until_eternity:frost_bitten");
                } catch (IOException exception) {
                    throw new AssertionError(exception);
                }
            }));
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
