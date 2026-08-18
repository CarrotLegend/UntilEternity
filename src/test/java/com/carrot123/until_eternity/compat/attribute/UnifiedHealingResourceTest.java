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

class UnifiedHealingResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));

    @Test
    void obscureHealingHandlerMixinIsCommonAndPreciselyScoped()
            throws IOException {
        JsonObject config = JsonParser.parseString(Files.readString(ROOT.resolve(
                Path.of("src", "main", "resources",
                        "until_eternity.mixins.json")))).getAsJsonObject();
        Set<String> common = config.getAsJsonArray("mixins").asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());
        Set<String> client = config.getAsJsonArray("client").asList().stream()
                .map(value -> value.getAsString())
                .collect(Collectors.toSet());

        String name = "compat.obscure_api.ObscureApiHealingPowerMixin";
        assertTrue(common.contains(name));
        assertFalse(client.contains(name));

        String mixin = source("mixin", "compat", "obscure_api",
                "ObscureApiHealingPowerMixin.java");
        assertTrue(mixin.contains("healEvent("));
        assertTrue(mixin.contains("at = @At(\"HEAD\")"));
        assertTrue(mixin.contains("cancellable = true"));
        assertTrue(mixin.contains("require = 1"));
        assertTrue(mixin.contains("callbackInfo.cancel()"));
        assertFalse(mixin.contains("event.setCanceled"));
        assertFalse(mixin.contains("regenerationEvent"));
    }

    @Test
    void foodDataChangesOnlyBothNaturalHealArguments() throws IOException {
        String mixin = source("mixin", "FoodDataMixin.java");
        assertTrue(mixin.contains("@WrapOperation("));
        assertTrue(mixin.contains(
                "target = \"Lnet/minecraft/world/entity/player/Player;heal(F)V\""));
        assertTrue(mixin.contains("require = 2"));
        assertTrue(mixin.contains("ModAttribute.NATURE_HEAL.get()"));
        assertTrue(mixin.contains("Operation<Void> original"));
        assertTrue(mixin.contains(
                "import com.carrot123.until_eternity.util.NaturalHealingMath;"));
        assertTrue(mixin.contains("original.call(player, modifiedAmount)"));
        assertTrue(mixin.contains("@WrapMethod("));
        assertFalse(mixin.contains("addExhaustion"));
        assertFalse(mixin.contains("tickTimer"));
        assertFalse(mixin.contains("naturalRegeneration"));
    }

    @Test
    void runtimeHealingHelperIsOutsideTheDefinedMixinPackage()
            throws IOException {
        Path helper = JAVA.resolve(Path.of(
                "util", "NaturalHealingMath.java"));
        Path oldHelper = JAVA.resolve(Path.of(
                "mixin", "NaturalHealingMath.java"));

        assertTrue(Files.exists(helper));
        assertFalse(Files.exists(oldHelper));
        String helperSource = Files.readString(helper);
        assertTrue(helperSource.contains(
                "package com.carrot123.until_eternity.util;"));
        assertTrue(helperSource.contains("public static float apply("));
    }

    @Test
    void productionMixinTreeContainsOnlyActualMixins() throws IOException {
        Path mixinRoot = JAVA.resolve("mixin");
        try (var files = Files.walk(mixinRoot)) {
            for (Path file : files
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                assertTrue(Files.readString(file).contains("@Mixin("),
                        () -> "Non-Mixin runtime class in defined Mixin package: "
                                + file);
            }
        }
    }

    @Test
    void converterReplacesOnlyModifierSourcesAndDoesNotMirrorTicks()
            throws IOException {
        String events = source("compat", "attribute",
                "UnifiedAttributeConversionEvents.java");
        assertTrue(events.contains("ObscureAPIAttributes.HEALING_POWER.get()"));
        assertTrue(events.contains("PuffishAttributes.HEALING"));
        assertTrue(events.contains("access.remove(source, modifier)"));
        assertTrue(events.contains("access.add(target,"));
        assertFalse(events.contains("LivingTickEvent"));
        assertFalse(events.contains("ObscureAPIAttributes.REGENERATION"));
        assertFalse(events.contains("ModAttribute.NATURE_HEAL"));
    }

    @Test
    void verifiedDependenciesStayPinned() throws IOException {
        String gradle = Files.readString(ROOT.resolve("build.gradle"));
        assertTrue(gradle.contains("obscure-api-638417:7171906"));
        assertTrue(gradle.contains("net.puffish:attributesmod:0.8.2+1.20:forge"));
        assertTrue(gradle.contains("lendercataclysm-551586:7908487"));
    }

    private static String source(String... parts) throws IOException {
        return Files.readString(JAVA.resolve(Path.of("", parts)));
    }
}
