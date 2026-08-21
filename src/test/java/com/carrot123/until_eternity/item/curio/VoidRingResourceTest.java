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
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoidRingResourceTest {
    private static final Path RESOURCES =
            Path.of("src", "main", "resources");

    @Test
    void warpedRingTagPreservesExistingRingsAndAddsVoidRing() throws IOException {
        Path tag = RESOURCES.resolve(Path.of(
                "data", "curios", "tags", "items", "warped_ring.json"));
        JsonObject root = JsonParser.parseString(Files.readString(tag)).getAsJsonObject();
        JsonArray values = root.getAsJsonArray("values");
        Set<String> actual = new HashSet<>();
        values.forEach(value -> actual.add(value.getAsString()));

        assertFalse(root.get("replace").getAsBoolean());
        assertEquals(Set.of(
                "until_eternity:ring_of_warped_magic",
                "until_eternity:advanced_ring_of_warped_magic",
                "until_eternity:ring_of_soul_craving",
                "until_eternity:ring_of_purity",
                "until_eternity:ring_of_warped_chanting",
                "until_eternity:ring_of_warped_cooling",
                "until_eternity:void_ring"
        ), actual);
    }

    @Test
    void modelAndTranslationsExistWithoutRecipe() throws IOException {
        Path model = RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "models", "item", "void_ring.json"));
        JsonObject modelJson = JsonParser.parseString(
                Files.readString(model)).getAsJsonObject();
        assertEquals(
                "until_eternity:item/void_ring",
                modelJson.getAsJsonObject("textures").get("layer0").getAsString());

        for (String language : Set.of("en_us.json", "zh_cn.json")) {
            Path lang = RESOURCES.resolve(Path.of(
                    "assets", "until_eternity", "lang", language));
            JsonObject translations = JsonParser.parseString(
                    Files.readString(lang)).getAsJsonObject();
            assertTrue(translations.has("item.until_eternity.void_ring"));
            assertFalse(translations.has("tooltip.until_eternity.void_ring.effect"));
            assertFalse(translations.has(
                    "death.attack.until_eternity.void_magic"));
            assertFalse(translations.has(
                    "death.attack.until_eternity.void_magic.player"));
        }

        assertFalse(Files.exists(RESOURCES.resolve(Path.of(
                "data", "until_eternity", "recipes", "void_ring.json"))));
    }

    @Test
    void mixinConfigDoesNotReferenceTheRemovedDarkWandMixin() throws IOException {
        Path config = RESOURCES.resolve("until_eternity.mixins.json");
        JsonObject root = JsonParser.parseString(
                Files.readString(config)).getAsJsonObject();
        Set<String> mixins = new HashSet<>();
        root.getAsJsonArray("mixins")
                .forEach(value -> mixins.add(value.getAsString()));

        assertEquals(Set.of(
                "ItemStackHoverNameMixin",
                "FoodDataMixin",
                "ManaEruptionStackingMixin",
                "SoulGreatSwordItemMixin",
                "compat.obscure_api.ObscureApiCriticalHitMixin",
                "compat.obscure_api.ObscureApiDodgeMixin",
                "compat.obscure_api.ObscureApiHealingPowerMixin",
                "compat.obscure_api.ObscureApiPenetrationMixin",
                "compat.terra_curio.TerraCurioArrowCriticalMixin",
                "compat.terra_curio.TerraCurioBreakSpeedMixin",
                "compat.terra_curio.TerraCurioDodgeChanceMixin",
                "compat.terra_curio.TerraCurioLivingHurtAttributesMixin",
                "compat.terra_curio.TerraCurioLivingDamageMixin",
                "compat.eeeabsmobs.BlockErosionPortalMixin",
                "compat.goety.DarkWandFocusCastMixin",
                "compat.goety.ServerLevelFocusEntityTickMixin",
                "compat.goetyrevelation.QuietusStarPropertiesMixin",
                "compat.irons_spellbooks.AbstractSpellCastingContextMixin",
                "compat.irons_spellbooks.AbstractSpellMixin",
                "compat.irons_spellbooks.CurioBaseItemMixin",
                "compat.irons_spellbooks.MagicManagerMixin",
                "compat.irons_spellbooks.ServerPlayerEventsMixin",
                "compat.irons_spellbooks.TeleportationAmuletItemMixin",
                "compat.thirst.PlayerThirstMixin"
        ), mixins);
        assertFalse(Files.exists(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "mixin", "GoetyDarkWandMixin.java")));
    }

    @Test
    void allLegacyVoidRingSpellDamageImplementationAndResourcesAreRemoved() {
        Path mainJava = Path.of(
                "src", "main", "java", "com", "carrot123", "until_eternity");
        for (String relativePath : Set.of(
                "compat/goety/FocusDamageContext.java",
                "compat/goety/FocusDamageFailure.java",
                "compat/goety/FocusDamageKind.java",
                "compat/goety/FocusDamageResolution.java",
                "compat/goety/GoetyFocusCastEventCompat.java",
                "compat/goety/GoetyFocusWhitelist.java",
                "compat/goety/VoidRingFocusContext.java",
                "event/VoidRingCombatEvents.java",
                "event/VoidRingDamageGuard.java",
                "event/VoidRingDamageLogic.java",
                "compat/goety/BoundedPendingQueue.java",
                "compat/goety/GoetySpellDamageClassifier.java",
                "compat/goety/GoetySpellDamageCompat.java",
                "compat/goety/GoetySpellDamageDecision.java",
                "compat/goety/GoetySpellDamageHandler.java",
                "compat/goety/PendingVoidHit.java",
                "compat/goety/VoidAccessoryChecker.java",
                "compat/goety/VoidRingDamageLogic.java",
                "damage/ModDamageTypes.java",
                "registry/ModTags.java"
        )) {
            assertFalse(Files.exists(mainJava.resolve(relativePath)), relativePath);
        }

        for (String relativePath : Set.of(
                "data/until_eternity/damage_type/void_magic.json",
                "data/until_eternity/tags/damage_type/goety_spell_damage_types.json",
                "data/until_eternity/tags/entity_type/goety_spell_damage_sources.json",
                "data/forge/tags/damage_type/is_magic.json",
                "data/minecraft/tags/damage_type/bypasses_armor.json",
                "data/minecraft/tags/damage_type/bypasses_cooldown.json",
                "data/minecraft/tags/damage_type/bypasses_effects.json",
                "data/minecraft/tags/damage_type/bypasses_enchantments.json",
                "data/minecraft/tags/damage_type/bypasses_invulnerability.json",
                "data/minecraft/tags/damage_type/bypasses_resistance.json",
                "data/minecraft/tags/damage_type/bypasses_shield.json"
        )) {
            assertFalse(Files.exists(RESOURCES.resolve(relativePath)), relativePath);
        }

        assertFalse(Files.exists(Path.of(
                "docs", "goety_spell_damage_whitelist.md")));
    }
}
