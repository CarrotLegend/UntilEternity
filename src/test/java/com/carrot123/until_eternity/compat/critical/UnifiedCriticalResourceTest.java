package com.carrot123.until_eternity.compat.critical;

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

class UnifiedCriticalResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123", "until_eternity"));

    @Test
    void obscureDependencyIsPinnedAndMandatoryOnBothSides() throws IOException {
        String gradle = Files.readString(ROOT.resolve("build.gradle"));
        String mods = Files.readString(ROOT.resolve(Path.of(
                "src", "main", "resources", "META-INF", "mods.toml")));
        assertTrue(gradle.contains("obscure-api-638417:7171906"));
        assertTrue(mods.contains("modId=\"obscure_api\""));
        assertTrue(mods.contains("versionRange=\"[18,19)\""));
        int dependency = mods.indexOf("modId=\"obscure_api\"");
        String block = mods.substring(dependency,
                Math.min(mods.length(), dependency + 180));
        assertTrue(block.contains("mandatory=true"));
        assertTrue(block.contains("side=\"BOTH\""));
    }

    @Test
    void allThreePreciseCompatibilityMixinsAreCommon() throws IOException {
        JsonObject config = JsonParser.parseString(Files.readString(ROOT.resolve(
                Path.of("src", "main", "resources",
                        "until_eternity.mixins.json")))).getAsJsonObject();
        Set<String> common = config.getAsJsonArray("mixins").asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());
        Set<String> client = config.getAsJsonArray("client").asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());
        assertTrue(common.contains("compat.terra_curio.TerraCurioLivingDamageMixin"));
        assertTrue(common.contains("compat.terra_curio.TerraCurioArrowCriticalMixin"));
        assertTrue(common.contains("compat.obscure_api.ObscureApiCriticalHitMixin"));
        assertFalse(client.stream().anyMatch(name -> name.contains("Critical")));
    }

    @Test
    void terraMixinsSkipOnlyOldCriticalBranches() throws IOException {
        String damage = source("mixin", "compat", "terra_curio",
                "TerraCurioLivingDamageMixin.java");
        String arrow = source("mixin", "compat", "terra_curio",
                "TerraCurioArrowCriticalMixin.java");
        assertTrue(damage.contains("ForgeEvents.class"));
        assertTrue(damage.contains("hasCustomAttribute"));
        assertTrue(damage.contains("require = 1"));
        assertFalse(damage.contains("CallbackInfo"));
        assertFalse(damage.contains("1.5"));
        assertTrue(arrow.contains("ordinal = 1"));
        assertTrue(arrow.contains("require = 1"));
        assertFalse(arrow.contains("setCritArrow"));
    }

    @Test
    void obscureMixinSkipsRandomOnlyAndLeavesMethodBodyIntact() throws IOException {
        String obscure = source("mixin", "compat", "obscure_api",
                "ObscureApiCriticalHitMixin.java");
        assertTrue(obscure.contains("criticalHitAndMagicResistanceEvent"));
        assertTrue(obscure.contains("Ljava/lang/Math;random()D"));
        assertTrue(obscure.contains("Double.POSITIVE_INFINITY"));
        assertTrue(obscure.contains("require = 1"));
        assertFalse(obscure.contains("CallbackInfo"));
        assertFalse(obscure.contains("setCanceled"));
    }

    @Test
    void corePerformsOneTerraRollAndOneDamageWrite() throws IOException {
        String handler = source("compat", "critical",
                "UnifiedCriticalHitHandler.java");
        assertTrue(handler.contains("priority = EventPriority.NORMAL"));
        assertTrue(handler.contains("DamageTypes.FELL_OUT_OF_WORLD"));
        assertTrue(handler.contains("DamageTypes.GENERIC_KILL"));
        assertTrue(handler.contains("getSource().getEntity()"));
        assertTrue(handler.contains("instanceof LivingEntity attacker"));
        assertTrue(handler.contains("ModAttributes.getCriticalChance()"));
        assertTrue(handler.contains("ObscureAPIAttributes.getCriticalDamage(attacker)"));
        assertEquals(1, count(handler, "nextFloat()"));
        assertEquals(1, count(handler, "event.setAmount("));
        assertFalse(handler.contains(".hurt("));
        assertFalse(handler.contains("1.5"));
    }

    @Test
    void conversionUsesLowestPriorityAndPreservesRingModifierObject()
            throws IOException {
        String events = source("compat", "critical",
                "CriticalAttributeConversionEvents.java");
        assertEquals(2, count(events, "priority = EventPriority.LOWEST"));
        assertTrue(events.contains("ObscureAPIAttributes.CRITICAL_HIT.get()"));
        assertTrue(events.contains("ModAttributes.getCriticalChance()"));
        assertTrue(events.contains("ModItems.RING_OF_GRUDGED.get()"));
        assertTrue(events.contains("ModAttribute.ADDITIONAL_CRITICAL_DAMAGE.get()"));
        assertTrue(events.contains("ObscureAPIAttributes.CRITICAL_DAMAGE.get()"));
        assertTrue(events.contains("event.addModifier(obscureCriticalDamage, modifier)"));
        assertTrue(events.contains("WARNED_UNSUPPORTED_OPERATION"));
    }

    private static String source(String... parts) throws IOException {
        return Files.readString(JAVA.resolve(Path.of("", parts)));
    }

    private static int count(String text, String needle) {
        return (text.length() - text.replace(needle, "").length())
                / needle.length();
    }
}
