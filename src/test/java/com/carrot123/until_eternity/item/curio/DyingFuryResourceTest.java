package com.carrot123.until_eternity.item.curio;

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

class DyingFuryResourceTest {
    private static final Path RESOURCES =
            Path.of("src", "main", "resources");

    @Test
    void modelTranslationsAndUserTextureExistWithoutRecipe()
            throws IOException {
        Path model = RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "models", "item",
                "dying_fury.json"));
        JsonObject modelJson = JsonParser.parseString(
                Files.readString(model)).getAsJsonObject();
        assertEquals("minecraft:item/generated",
                modelJson.get("parent").getAsString());
        assertEquals("until_eternity:item/dying_fury",
                modelJson.getAsJsonObject("textures")
                        .get("layer0").getAsString());

        for (String language : Set.of("en_us.json", "zh_cn.json")) {
            Path lang = RESOURCES.resolve(Path.of(
                    "assets", "until_eternity", "lang", language));
            JsonObject translations = JsonParser.parseString(
                    Files.readString(lang)).getAsJsonObject();
            assertTrue(translations.has(
                    "item.until_eternity.dying_fury"));
            assertTrue(translations.has(
                    "tooltip.until_eternity.dying_fury.effect"));
        }

        assertFalse(Files.exists(RESOURCES.resolve(Path.of(
                "data", "until_eternity", "recipes",
                "dying_fury.json"))));
        Path texture = RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "textures", "item",
                "dying_fury.png"));
        assertTrue(Files.exists(texture));
        byte[] signature = Files.readAllBytes(texture);
        assertTrue(signature.length >= 8);
        assertEquals((byte) 0x89, signature[0]);
        assertEquals((byte) 'P', signature[1]);
        assertEquals((byte) 'N', signature[2]);
        assertEquals((byte) 'G', signature[3]);
    }

    @Test
    void combatHandlerUsesLivingHurtEventLikeCursedScroll()
            throws IOException {
        String source = Files.readString(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "event",
                "DyingFuryCombatEvents.java"));

        // 千咒卷轴模式：LivingHurtEvent，无优先级，source.getEntity() instanceof Player
        assertTrue(source.contains("LivingHurtEvent"),
                "必须使用 LivingHurtEvent（而非 LivingDamageEvent）");
        assertTrue(source.contains(
                "getSource().getEntity() instanceof ServerPlayer"),
                "必须使用 source.getEntity() 检测玩家来源（千咒卷轴模式）");
        assertFalse(source.contains("LivingDamageEvent"),
                "不应再使用 LivingDamageEvent");
        assertFalse(source.contains("LivingAttackEvent"),
                "不应使用 LivingAttackEvent");
        assertFalse(source.contains("resolvePlayerAttacker"),
                "旧版 resolvePlayerAttacker 应已移除");
    }
}
