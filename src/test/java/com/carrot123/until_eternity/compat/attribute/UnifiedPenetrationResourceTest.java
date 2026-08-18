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

class UnifiedPenetrationResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123", "until_eternity"));

    @Test
    void converterUsesRegistryOnlyGoetySourcesAndNoToughnessTarget()
            throws IOException {
        String events = source("compat", "attribute",
                "UnifiedAttributeConversionEvents.java");
        String conversions = source("compat", "attribute",
                "AttributeModifierConversions.java");

        assertEquals(2, count(events, "priority = EventPriority.LOWEST"));
        assertTrue(events.contains("ForgeRegistries.ATTRIBUTES.getValue(sourceId)"));
        assertTrue(events.contains("access.remove(source, modifier)"));
        assertTrue(events.contains("AttributeModifierConversions.percentageShred"));
        assertTrue(conversions.contains("sourceAttributeId"));
        assertTrue(conversions.contains("source.getId()"));
        assertTrue(conversions.contains("targetAttributeId"));
        assertTrue(conversions.contains("Operation.MULTIPLY_BASE"));
        assertFalse(events.contains("TOUGHNESS_SHRED"));
        assertFalse(conversions.contains("TOUGHNESS_SHRED"));
    }

    @Test
    void proofOfSpurnerTargetsOnlyPuffishArmorAndProtectionShred()
            throws IOException {
        String profile = source("item", "curio", "CurioAttributeProfile.java");
        String proof = profile.substring(profile.indexOf("PROOF_OF_SPURNER"));

        assertTrue(proof.contains("PuffishAttributesCompat.ARMOR_SHRED"));
        assertTrue(proof.contains("PuffishAttributesCompat.PROTECTION_SHRED"));
        assertTrue(proof.contains("\"armor_shred\", 1.0D"));
        assertTrue(proof.contains("\"protection_shred\", 0.50D"));
        assertFalse(proof.contains("GoetyRevelationAttributesCompat.ARMOR_PENETRATION"));
        assertFalse(proof.contains("GoetyRevelationAttributesCompat.ENCHANTMENT_PIERCING"));
        assertFalse(proof.contains("TOUGHNESS_SHRED"));
    }

    @Test
    void obscureMixinDisablesOnlyThePenetrationHelper() throws IOException {
        String mixin = source("mixin", "compat", "obscure_api",
                "ObscureApiPenetrationMixin.java");
        assertTrue(mixin.contains(
                "getPenetration(Lnet/minecraft/world/entity/LivingEntity;)F"));
        assertTrue(mixin.contains("at = @At(\"HEAD\")"));
        assertTrue(mixin.contains("callback.setReturnValue(0.0F)"));
        assertTrue(mixin.contains("require = 1"));
        assertFalse(mixin.contains("applyPenetration"));
        assertFalse(mixin.contains("setCanceled"));

        JsonObject config = JsonParser.parseString(Files.readString(ROOT.resolve(
                Path.of("src", "main", "resources",
                        "until_eternity.mixins.json")))).getAsJsonObject();
        Set<String> common = config.getAsJsonArray("mixins").asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());
        Set<String> client = config.getAsJsonArray("client").asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());
        assertTrue(common.contains(
                "compat.obscure_api.ObscureApiPenetrationMixin"));
        assertFalse(client.contains(
                "compat.obscure_api.ObscureApiPenetrationMixin"));
        assertTrue(common.stream().noneMatch(name ->
                name.toLowerCase().contains("goety")
                        && name.toLowerCase().contains("penetration")));
    }

    @Test
    void terraArmorPassIsOutsidePenetrationConversion() throws IOException {
        String events = source("compat", "attribute",
                "UnifiedAttributeConversionEvents.java");
        String terra = source("compat", "TerraCurioCompat.java");
        assertFalse(events.contains("ARMOR_PASS"));
        assertTrue(terra.contains("armor_pass"));
    }

    private static String source(String... parts) throws IOException {
        return Files.readString(JAVA.resolve(Path.of("", parts)));
    }

    private static int count(String text, String needle) {
        return (text.length() - text.replace(needle, "").length())
                / needle.length();
    }
}
