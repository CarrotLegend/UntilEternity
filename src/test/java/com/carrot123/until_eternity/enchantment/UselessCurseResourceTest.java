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

class UselessCurseResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path MAIN_JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of(
            "src", "main", "resources"));

    @Test
    void registersARealLevelOneDiscoverableCurse() throws IOException {
        String registry = Files.readString(MAIN_JAVA.resolve(Path.of(
                "enchantment", "ModEnchantments.java")));
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "enchantment", "UselessCurseEnchantment.java")));

        assertTrue(registry.contains(
                "ENCHANTMENTS.register(\"useless_curse\", "
                        + "UselessCurseEnchantment::new)"));
        assertTrue(source.contains("EnchantmentCategory.BREAKABLE"));
        assertTrue(source.contains("EquipmentSlot.values()"));
        assertTrue(source.contains("public int getMinLevel()"));
        assertTrue(source.contains("public int getMaxLevel()"));
        assertTrue(source.contains("public boolean isCurse()"));
        assertTrue(source.contains("public boolean isDiscoverable()"));
        assertTrue(source.contains("public boolean isTreasureOnly()"));
        assertTrue(source.contains("return true;"));
        assertTrue(source.contains("return false;"));
        assertFalse(source.contains("isTradeable()"));
        assertFalse(source.contains("isAllowedOnBooks()"));
    }

    @Test
    void implementationHasNoGameplayEffectHooks() throws IOException {
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "enchantment", "UselessCurseEnchantment.java")));
        for (String forbidden : Set.of(
                "doPostAttack", "doPostHurt", "getDamageProtection",
                "getDamageBonus", "TickEvent", "AttributeModifier",
                "MobEffect", "CompoundTag", "ItemAttributeModifierEvent")) {
            assertFalse(source.contains(forbidden), forbidden);
        }

        String gameplayEvents = Files.readString(MAIN_JAVA.resolve(Path.of(
                "event", "EnchantmentEventHandler.java")));
        assertFalse(gameplayEvents.contains("USELESS_CURSE"));
        assertFalse(gameplayEvents.contains("useless_curse"));
    }

    @Test
    void tooltipIsClientOnlyAndReadsBothEnchantmentNbtForms()
            throws IOException {
        String tooltip = Files.readString(MAIN_JAVA.resolve(Path.of(
                "event", "UselessCurseTooltipEvents.java")));

        assertTrue(tooltip.contains("value = Dist.CLIENT"));
        assertTrue(tooltip.contains("ItemTooltipEvent"));
        assertTrue(tooltip.contains("ActualEnchantmentLevel.read("));
        assertTrue(tooltip.contains("ChatFormatting.GRAY"));
        assertFalse(tooltip.contains("TickEvent"));
        assertFalse(tooltip.contains(".put("));
    }

    @Test
    void translationsAreExact() throws IOException {
        JsonObject english = readLanguage("en_us.json");
        JsonObject chinese = readLanguage("zh_cn.json");

        assertEquals("Useless Curse", english.get(
                "enchantment.until_eternity.useless_curse").getAsString());
        assertEquals("Exactly what it says. It does absolutely nothing.",
                english.get("enchantment.until_eternity.useless_curse.desc")
                        .getAsString());
        assertEquals("无用诅咒", chinese.get(
                "enchantment.until_eternity.useless_curse").getAsString());
        assertEquals("字面意思，没有任何效果。", chinese.get(
                "enchantment.until_eternity.useless_curse.desc")
                        .getAsString());
    }

    @Test
    void noDedicatedLootInjectionWasAdded() throws IOException {
        try (var paths = Files.walk(RESOURCES.resolve("data"))) {
            assertTrue(paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .noneMatch(path -> {
                try {
                    return Files.readString(path).contains("useless_curse");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            }));
        }
        try (var paths = Files.walk(MAIN_JAVA.resolve("loot"))) {
            assertTrue(paths.filter(Files::isRegularFile).noneMatch(path -> {
                try {
                    return Files.readString(path).contains("useless_curse");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            }));
        }
    }

    private static JsonObject readLanguage(String file) throws IOException {
        return JsonParser.parseString(Files.readString(RESOURCES.resolve(
                Path.of("assets", "until_eternity", "lang", file))))
                .getAsJsonObject();
    }
}
