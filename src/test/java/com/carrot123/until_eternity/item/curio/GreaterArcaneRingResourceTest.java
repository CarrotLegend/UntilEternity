package com.carrot123.until_eternity.item.curio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreaterArcaneRingResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path RESOURCES = ROOT.resolve(
            Path.of("src", "main", "resources"));

    @Test
    void registrationUsesExpectedIronManaAttributesAndLimit() throws IOException {
        String source = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "item", "ModItems.java")));
        assertTrue(source.contains("ITEMS.register(\"greater_arcane_ring\""));
        assertTrue(source.contains("Rarity.RARE).fireResistant()"));
        assertTrue(source.contains("\"greater_arcane_ring/max_mana_flat\",400.0D,AttributeModifier.Operation.ADDITION"));
        assertTrue(source.contains("\"greater_arcane_ring/max_mana_percent\",0.10D,AttributeModifier.Operation.MULTIPLY_BASE"));
        assertTrue(source.contains("AttributeRegistry.MAX_MANA::get"));
        assertTrue(source.contains("mana_regeneration\""));
        assertTrue(source.contains("MULTIPLY_BASE)),3,"));
    }

    @Test
    void modifierIdsAreStableAndDistinctBySlotAndDefinition() {
        UUID firstSlot = UUID.fromString(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID secondSlot = UUID.fromString(
                "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID flat = CurioModifierId.create(
                firstSlot, "greater_arcane_ring/max_mana_flat");
        assertEquals(flat, CurioModifierId.create(
                firstSlot, "greater_arcane_ring/max_mana_flat"));
        assertNotEquals(flat, CurioModifierId.create(
                firstSlot, "greater_arcane_ring/max_mana_percent"));
        assertNotEquals(flat, CurioModifierId.create(
                secondSlot, "greater_arcane_ring/max_mana_flat"));
    }

    @Test
    void resourcesAndDependenciesAreComplete() throws IOException {
        JsonObject model = json(RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "models", "item",
                "greater_arcane_ring.json")));
        assertEquals("until_eternity:item/greater_arcane_ring",
                model.getAsJsonObject("textures").get("layer0").getAsString());
        BufferedImage texture = ImageIO.read(RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "textures", "item",
                "greater_arcane_ring.png")).toFile());
        assertEquals(16, texture.getWidth());
        assertEquals(16, texture.getHeight());
        assertFalse(Files.exists(RESOURCES.resolve("assets/model.png")));

        JsonObject english = json(RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "lang", "en_us.json")));
        JsonObject chinese = json(RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "lang", "zh_cn.json")));
        for (JsonObject language : Set.of(english, chinese)) {
            assertTrue(language.has("item.until_eternity.greater_arcane_ring"));
            assertTrue(language.has("tooltip.until_eternity.greater_arcane_ring.mana_regeneration"));
            assertTrue(language.has("tooltip.until_eternity.resonance_armor.mana_power"));
        }
        assertTrue(Files.readString(RESOURCES.resolve(Path.of(
                "data", "curios", "tags", "items", "magic_ring.json")))
                .contains("until_eternity:greater_arcane_ring"));
        assertFalse(Files.exists(RESOURCES.resolve(Path.of(
                "data", "until_eternity", "recipes",
                "greater_arcane_ring.json"))));

        String gradle = Files.readString(ROOT.resolve("build.gradle"));
        String mods = Files.readString(RESOURCES.resolve("META-INF/mods.toml"));
        assertTrue(gradle.contains(
                "curse.maven:thirst-was-taken-679270:6660408"));
        assertTrue(mods.contains("modId=\"thirst\""));
        assertTrue(mods.contains(
                "versionRange=\"[1.20.1-1.4.0]\""));
    }

    @Test
    void tickEffectsAndMixinsUseExactServerEntrypoints() throws IOException {
        String event = source("event/GreaterArcaneRingEvents.java");
        assertTrue(event.contains("TickEvent.Phase.END"));
        assertTrue(event.contains("player.tickCount % 20"));
        assertTrue(event.contains("addMana(MANA_PER_SECOND)"));
        assertTrue(event.contains("countEquipped("));

        String food = source("mixin/FoodDataMixin.java");
        assertTrue(food.contains(
                "tick(Lnet/minecraft/world/entity/player/Player;)V"));
        assertTrue(food.contains("require = 1"));
        assertFalse(food.contains("remap = false"));

        String thirst = source("mixin/compat/thirst/PlayerThirstMixin.java");
        assertTrue(thirst.contains(
                "tick(Lnet/minecraft/world/entity/player/Player;)V"));
        assertTrue(thirst.contains("remap = false"));
        assertTrue(thirst.contains("require = 1"));
        assertTrue(thirst.contains("ModCapabilities.PLAYER_THIRST"));
        assertTrue(thirst.contains("updateThirstData(player)"));

        String mixins = Files.readString(RESOURCES.resolve(
                "until_eternity.mixins.json"));
        assertTrue(mixins.contains("FoodDataMixin"));
        assertTrue(mixins.contains("compat.thirst.PlayerThirstMixin"));
    }

    @Test
    void imFullOnlyScansMainInventoryAndOffhandWithoutTooltip() throws IOException {
        String helper = source("item/ImFullInventoryHelper.java");
        assertTrue(helper.contains("getInventory().items"));
        assertTrue(helper.contains("getInventory().offhand"));
        assertFalse(helper.contains("armor"));
        assertFalse(helper.contains("CuriosApi"));
        assertFalse(helper.contains("getMainHandItem"));
        String items = source("item/ModItems.java");
        assertTrue(items.contains("IMFULL = ITEMS.register(\"imfull\""));
        assertFalse(items.contains("tooltip.until_eternity.imfull"));
    }

    private static String source(String relative) throws IOException {
        return Files.readString(ROOT.resolve(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity")).resolve(relative));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
