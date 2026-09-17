package com.carrot123.until_eternity.contract;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class EnigmaticAndArmorRendContractTest {
    private static String source(String relativePath) throws IOException {
        return Files.readString(Path.of(System.getProperty("user.dir"), relativePath));
    }

    @Test
    void optionalDependenciesAndMixinGatesAreDeclared() throws IOException {
        String mods = source("src/main/resources/META-INF/mods.toml");
        String plugin = source("src/main/java/com/carrot123/until_eternity/compat/mixin/UntilEternityMixinPlugin.java");
        assertTrue(mods.contains("modId=\"enigmaticlegacy\""));
        assertTrue(mods.contains("modId=\"enigmaticdelicacy\""));
        assertTrue(plugin.contains("isModLoaded(\"enigmaticlegacy\")\n                    && isModLoaded(\"enigmaticdelicacy\")"));
    }

    @Test
    void infinimealUsesExactIdsAndVanillaVirtualGrowthMethods() throws IOException {
        String compat = source("src/main/java/com/carrot123/until_eternity/compat/enigmaticdelicacy/InfinimealGrowthCompat.java");
        assertTrue(compat.contains("\"enigmatic_bush\""));
        assertTrue(compat.contains("\"astral_sapling\""));
        assertTrue(compat.contains("crop.growCrops(level, pos, state)"));
        assertTrue(compat.contains("sapling.advanceTree(serverLevel, pos, state, serverLevel.random)"));
    }

    @Test
    void cursedScrollKeepsConfigKeyAndOnlyReplacesItsCurseCountExpression() throws IOException {
        String configMixin = source("src/main/java/com/carrot123/until_eternity/mixin/compat/enigmaticlegacy/CursedScrollConfigMixin.java");
        String miningMixin = source("src/main/java/com/carrot123/until_eternity/mixin/compat/enigmaticlegacy/CursedScrollMiningMixin.java");
        String events = source("src/main/java/com/carrot123/until_eternity/compat/enigmaticlegacy/CursedScrollAttackSpeedEvents.java");
        assertTrue(configMixin.contains("Attack speed increase provided by Scroll of a Thousand Curses"));
        assertTrue(miningMixin.contains("SuperpositionHandler;getCurseAmount"));
        assertTrue(events.contains("CursedScroll.miningBoost.getValue().asModifier()"));
        assertTrue(events.contains("AttributeModifier.Operation.MULTIPLY_TOTAL"));
        assertTrue(events.contains("tooltip.enigmaticlegacy.cursed_scroll2"));
        assertTrue(events.contains("tooltip.enigmaticlegacy.cursed_scroll8"));
    }

    @Test
    void armorRendUsesVanillaDiscoverableGenerationAndCreativeBooks() throws IOException {
        String enchantment = source("src/main/java/com/carrot123/until_eternity/enchantment/ArmorRendEnchantment.java");
        String eligibility = source("src/main/java/com/carrot123/until_eternity/enchantment/ArmorRendItemEligibility.java");
        String mixins = source("src/main/resources/until_eternity.mixins.json");
        assertTrue(enchantment.matches(
                "(?s).*public boolean isDiscoverable\\(\\) \\{\\s*return true;.*"));
        assertTrue(enchantment.matches(
                "(?s).*public boolean isTradeable\\(\\) \\{\\s*return false;.*"));
        assertTrue(enchantment.matches(
                "(?s).*public boolean isAllowedOnBooks\\(\\) \\{\\s*return true;.*"));
        assertTrue(enchantment.contains("stack.is(Items.BOOK) || canEnchant(stack)"));
        assertTrue(enchantment.contains("public boolean allowedInCreativeTab("));
        assertTrue(enchantment.contains("item == Items.ENCHANTED_BOOK && isAllowedOnBooks()"));
        assertTrue(eligibility.contains("item instanceof TieredItem"));
        assertTrue(eligibility.contains("item instanceof ProjectileWeaponItem"));
        assertTrue(eligibility.contains("item instanceof TridentItem"));
        assertTrue(eligibility.contains("item instanceof ShieldItem"));
        assertTrue(eligibility.contains("item instanceof ArmorItem"));
        assertTrue(eligibility.contains("item instanceof BlockItem"));
        assertTrue(eligibility.contains("CuriosApi.getCurio(stack).isPresent()"));
        assertFalse(mixins.contains("EnchantmentMenuArmorRendMixin"));
        assertFalse(mixins.contains("EnchantmentHelperArmorRendMixin"));
        assertFalse(Files.exists(Path.of(System.getProperty("user.dir"),
                "src/main/java/com/carrot123/until_eternity/enchantment/EnchantingTableSelectionContext.java")));
    }
}
