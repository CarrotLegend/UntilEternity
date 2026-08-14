package com.carrot123.until_eternity.compat.eeeabsmobs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetherworldKatanaResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of(
            "src", "main", "resources"));

    @Test
    void exactDependencyAndRuntimeMetadataArePinned() throws IOException {
        String build = Files.readString(ROOT.resolve("build.gradle"));
        String mods = Files.readString(RESOURCES.resolve(
                Path.of("META-INF", "mods.toml")));
        assertTrue(build.contains(
                "curse.maven:eeeabs-mobs-921600:6095880"));
        assertTrue(mods.contains("modId=\"eeeabsmobs\""));
        assertTrue(mods.contains("versionRange=\"[1.20.1-0.97]\""));
        assertTrue(mods.contains("mandatory=true"));
    }

    @Test
    void eventUsesExactItemAndOneFinalDamageEvent() throws IOException {
        String source = Files.readString(JAVA.resolve(Path.of(
                "event", "NetherworldKatanaEvents.java")));
        assertTrue(source.contains(
                "com.eeeab.eeeabsmobs.sever.init.ItemInit"));
        assertTrue(source.contains("ItemInit.THE_NETHERWORLD_KATANA.get()"));
        assertTrue(source.contains("LivingDamageEvent"));
        assertTrue(source.contains("EventPriority.LOWEST"));
        assertTrue(source.contains("getDirectEntity() != player"));
        assertTrue(source.contains("boolean wasScarred"));
        assertTrue(source.contains("player.getRandom().nextFloat()"));
        assertFalse(source.contains("target.hurt("));
        assertFalse(source.contains("LivingHurtEvent"));
    }

    @Test
    void mainhandCriticalChancePreservesUpstreamModifiers()
            throws IOException {
        String source = Files.readString(JAVA.resolve(Path.of(
                "event", "NetherworldKatanaEvents.java")));
        assertTrue(source.contains("EquipmentSlot.MAINHAND"));
        assertTrue(source.contains("ModAttributes.getCriticalChance()"));
        assertTrue(source.contains("CRITICAL_CHANCE_AMOUNT = 0.25D"));
        assertTrue(source.contains("AttributeModifier.Operation.ADDITION"));
        assertTrue(source.contains(
                "5d151384-9131-3e77-9d04-d5612e819a6f"));
        assertFalse(source.contains("clearModifiers"));
        assertFalse(source.contains("randomUUID"));
        assertEquals(UUID.fromString(
                        "5d151384-9131-3e77-9d04-d5612e819a6f"),
                UUID.nameUUIDFromBytes(
                        "until_eternity:netherworld_katana/critical_chance"
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void effectTooltipTranslationsAndIconExist() throws IOException {
        for (String language : new String[]{"en_us.json", "zh_cn.json"}) {
            JsonObject translations = JsonParser.parseString(
                    Files.readString(RESOURCES.resolve(Path.of(
                            "assets", "until_eternity", "lang", language))))
                    .getAsJsonObject();
            assertTrue(translations.has(
                    "effect.until_eternity.immortal_scar"));
            assertTrue(translations.has(
                    "effect.until_eternity.immortal_scar.description"));
            assertTrue(translations.has(
                    "tooltip.until_eternity.netherworld_katana.critical_chance"));
            assertTrue(translations.has(
                    "tooltip.until_eternity.netherworld_katana.immortal_scar"));
        }

        Path icon = RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "textures", "mob_effect",
                "immortal_scar.png"));
        BufferedImage image = ImageIO.read(icon.toFile());
        assertNotNull(image);
        assertEquals(18, image.getWidth());
        assertEquals(18, image.getHeight());
        assertTrue(image.getColorModel().hasAlpha());
        assertTrue(Files.isRegularFile(ROOT.resolve(Path.of(
                "tools", "generate_immortal_scar_icon.py"))));
    }
}
