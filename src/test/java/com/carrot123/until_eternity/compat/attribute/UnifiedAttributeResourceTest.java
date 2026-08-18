package com.carrot123.until_eternity.compat.attribute;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnifiedAttributeResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));

    @Test
    void dependencyVersionsRemainPinnedToVerifiedArtifacts()
            throws IOException {
        String gradle = Files.readString(ROOT.resolve("build.gradle"));
        assertTrue(gradle.contains(
                "terra-curio-1027625:7449534"));
        assertTrue(gradle.contains(
                "net.puffish:attributesmod:0.8.2+1.20:forge"));
        assertTrue(gradle.contains(
                "obscure-api-638417:7171906"));
    }

    @Test
    void converterIncludesHealingWithoutTouchingOtherHealingAttributes()
            throws IOException {
        String events = source("compat", "attribute",
                "UnifiedAttributeConversionEvents.java");
        assertEquals(2, count(events,
                "priority = EventPriority.LOWEST"));
        assertTrue(events.contains("ObscureAPIAttributes.DODGE.get()"));
        assertTrue(events.contains("ObscureAPIAttributes.HEALING_POWER.get()"));
        assertTrue(events.contains("ObscureAPIAttributes.PENETRATION.get()"));
        assertTrue(events.contains(
                "GoetyRevelationAttributesCompat.ARMOR_PENETRATION"));
        assertTrue(events.contains(
                "GoetyRevelationAttributesCompat.ENCHANTMENT_PIERCING"));
        assertTrue(events.contains("PuffishAttributes.ARMOR_SHRED"));
        assertTrue(events.contains("PuffishAttributes.PROTECTION_SHRED"));
        assertFalse(events.contains("PuffishAttributes.TOUGHNESS_SHRED"));
        assertTrue(events.contains("PuffishAttributes.HEALING"));
        assertTrue(events.contains("ModAttributes.DODGE_CHANCE.get()"));
        assertTrue(events.contains("ModAttributes.RANGED_DAMAGE.get()"));
        assertTrue(events.contains("PuffishAttributes.RANGED_DAMAGE"));
        assertTrue(events.contains("ModAttributes.MAGIC_DAMAGE.get()"));
        assertTrue(events.contains("PuffishAttributes.MAGIC_DAMAGE"));
        assertTrue(events.contains("ModAttributes.MINING_SPEED.get()"));
        assertTrue(events.contains("PuffishAttributes.BREAKING_SPEED"));
        assertFalse(events.contains("PuffishAttributes.MINING_SPEED"));
        assertFalse(events.contains("ObscureAPIAttributes.ACCURACY"));
        assertFalse(events.contains("ObscureAPIAttributes.MAGIC_DAMAGE"));
        assertFalse(events.contains("ObscureAPIAttributes.REGENERATION"));
        assertFalse(events.contains("ModAttribute.NATURE_HEAL"));
        assertTrue(events.contains("WARNED_TERRA_MIXED_OPERATIONS"));
    }

    @Test
    void fourExecutionMixinsAreCommonAndPreciselyScoped() throws IOException {
        JsonObject config = JsonParser.parseString(Files.readString(ROOT.resolve(
                Path.of("src", "main", "resources",
                        "until_eternity.mixins.json")))).getAsJsonObject();
        Set<String> common = config.getAsJsonArray("mixins").asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());
        Set<String> client = config.getAsJsonArray("client").asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());

        for (String mixin : Set.of(
                "compat.obscure_api.ObscureApiDodgeMixin",
                "compat.terra_curio.TerraCurioBreakSpeedMixin",
                "compat.terra_curio.TerraCurioDodgeChanceMixin",
                "compat.terra_curio.TerraCurioLivingHurtAttributesMixin")) {
            assertTrue(common.contains(mixin));
            assertFalse(client.contains(mixin));
        }

        String obscure = source("mixin", "compat", "obscure_api",
                "ObscureApiDodgeMixin.java");
        assertTrue(obscure.contains("parryAndDodgeEvent"));
        assertTrue(obscure.contains("ordinal = 1"));
        assertTrue(obscure.contains("Double.POSITIVE_INFINITY"));
        assertTrue(obscure.contains("require = 1"));
        assertFalse(obscure.contains("CallbackInfo"));
        assertFalse(obscure.contains("setCanceled"));

        String damage = source("mixin", "compat", "terra_curio",
                "TerraCurioLivingHurtAttributesMixin.java");
        assertTrue(damage.contains("applyMagicDamage"));
        assertTrue(damage.contains("applyRangedDamage"));
        assertEquals(2, count(damage, "return amount;"));
        assertEquals(2, count(damage, "require = 1"));
        assertFalse(damage.contains("CallbackInfo"));

        String breakSpeed = source("mixin", "compat", "terra_curio",
                "TerraCurioBreakSpeedMixin.java");
        assertTrue(breakSpeed.contains("breakSpeed("));
        assertTrue(breakSpeed.contains("hasCustomAttribute"));
        assertTrue(breakSpeed.contains("ordinal = 0"));
        assertFalse(breakSpeed.contains("setNewSpeed"));
        assertFalse(breakSpeed.contains("CallbackInfo"));

        String dodge = source("mixin", "compat", "terra_curio",
                "TerraCurioDodgeChanceMixin.java");
        assertTrue(dodge.contains("applyDodge("));
        assertTrue(dodge.contains("AttributeInstance;getValue()D"));
        assertTrue(dodge.contains("ModifyExpressionValue"));
        assertFalse(dodge.contains("nextFloat"));
        assertFalse(dodge.contains("Math.random"));
    }

    @Test
    void priorCriticalUnificationRemainsRegistered() throws IOException {
        String config = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "resources", "until_eternity.mixins.json")));
        assertTrue(config.contains("ObscureApiCriticalHitMixin"));
        assertTrue(config.contains("TerraCurioArrowCriticalMixin"));
        assertTrue(config.contains("TerraCurioLivingDamageMixin"));
        assertTrue(Files.isRegularFile(JAVA.resolve(Path.of(
                "compat", "critical", "UnifiedCriticalHitHandler.java"))));
    }

    private static String source(String... parts) throws IOException {
        return Files.readString(JAVA.resolve(Path.of("", parts)));
    }

    private static int count(String text, String needle) {
        return (text.length() - text.replace(needle, "").length())
                / needle.length();
    }
}
