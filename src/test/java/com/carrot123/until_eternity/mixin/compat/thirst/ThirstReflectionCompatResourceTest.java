package com.carrot123.until_eternity.mixin.compat.thirst;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThirstReflectionCompatResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path MIXIN = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123", "until_eternity",
            "mixin", "compat", "thirst", "ThirstReflectionCompatMixin.java"));

    @Test
    void narrowlyDispatchesOnlyTheInvalidDefaultDispenserReceiver() throws IOException {
        String source = Files.readString(MIXIN);

        assertTrue(source.contains("@Mixin(value = ReflectionUtil.class, remap = false)"));
        assertTrue(source.contains(
                "fuckYouReflections(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"));
        assertTrue(source.contains("at = @At(\"HEAD\")"));
        assertTrue(source.contains("cancellable = true"));
        assertTrue(source.contains("require = 1"));
        assertTrue(source.contains("method != null"));
        assertTrue(source.contains("receiver instanceof DispenseItemBehavior behavior"));
        assertTrue(source.contains(
                "method.getDeclaringClass() == DefaultDispenseItemBehavior.class"));
        assertTrue(source.contains("!method.getDeclaringClass().isInstance(receiver)"));
        assertTrue(source.contains("args != null"));
        assertTrue(source.contains("args.length == 2"));
        assertTrue(source.contains("args[0] instanceof BlockSource source"));
        assertTrue(source.contains("args[1] instanceof ItemStack stack"));
        assertEquals(1, occurrences(source,
                "cir.setReturnValue(behavior.dispense(source, stack));"));
        assertFalse(source.contains("net.mehvahdjukaar"));
        assertFalse(source.contains("moonlight"));
        assertFalse(source.contains("lambda$"));
    }

    @Test
    void mixinIsRegisteredOnceWithoutAddingMoonlightDependency() throws IOException {
        JsonObject config = JsonParser.parseString(Files.readString(ROOT.resolve(Path.of(
                "src", "main", "resources", "until_eternity.mixins.json"))))
                .getAsJsonObject();
        long registrations = config.getAsJsonArray("mixins").asList().stream()
                .filter(value -> value.getAsString().equals(
                        "compat.thirst.ThirstReflectionCompatMixin"))
                .count();
        String build = Files.readString(ROOT.resolve("build.gradle"));

        assertEquals(1L, registrations);
        assertTrue(build.contains(
                "curse.maven:thirst-was-taken-679270:6660408"));
        assertFalse(build.toLowerCase().contains("moonlight"));
    }

    private static int occurrences(String source, String needle) {
        return (source.length() - source.replace(needle, "").length())
                / needle.length();
    }
}
