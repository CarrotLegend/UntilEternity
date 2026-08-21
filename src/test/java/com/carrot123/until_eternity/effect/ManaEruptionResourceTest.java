package com.carrot123.until_eternity.effect;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManaEruptionResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path MAIN_JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));
    private static final Path RESOURCES =
            ROOT.resolve(Path.of("src", "main", "resources"));

    @Test
    void effectKeepsIronsPowerAndUsesTheExistingFocusDamageAttribute()
            throws IOException {
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "effect", "ManaEruptionEffect.java")));

        assertTrue(source.contains("MobEffectCategory.BENEFICIAL"));
        assertTrue(source.contains("0x7047FF"));
        assertTrue(source.contains(
                "AttributeRegistry.SPELL_POWER.get()"));
        assertTrue(source.contains(
                "ModAttributes.FOCUS_DAMAGE.get()"));
        assertEquals(
                1,
                occurrences(
                        source,
                        "AttributeModifier.Operation.ADDITION"));
        assertEquals(
                1,
                occurrences(
                        source,
                        "AttributeModifier.Operation.MULTIPLY_BASE"));
        assertTrue(source.contains(
                "\"1c73d95a-fafe-38e0-973e-829f64787e33\""));
        assertTrue(source.contains(
                "\"d0b0a8a2-03be-38ee-b6f0-225d3e6ba086\""));
        assertTrue(source.contains(
                "FOCUS_DAMAGE_AMOUNT_PER_LEVEL = 0.10D"));
        assertFalse(source.contains("SPELL_POTENCY"));
        assertFalse(source.contains("com.Polarice3.Goety.init.ModAttributes"));
        assertFalse(source.contains("applyEffectTick"));
        assertFalse(source.contains("randomUUID"));
        assertEquals(
                UUID.fromString("1c73d95a-fafe-38e0-973e-829f64787e33"),
                UUID.nameUUIDFromBytes(
                        "until_eternity:mana_eruption/irons_spell_power"
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertEquals(
                UUID.fromString("d0b0a8a2-03be-38ee-b6f0-225d3e6ba086"),
                UUID.fromString(ManaEruptionEffect.FOCUS_DAMAGE_UUID));
    }

    @Test
    void registriesAndPotionVariantsUseTheSpecifiedIdsAndDurations()
            throws IOException {
        String effects = Files.readString(MAIN_JAVA.resolve(Path.of(
                "registry", "ModMobEffects.java")));
        String potions = Files.readString(MAIN_JAVA.resolve(Path.of(
                "registry", "ModPotions.java")));
        String mod = Files.readString(MAIN_JAVA.resolve(
                "until_eternity.java"));

        assertTrue(effects.contains(
                "DeferredRegister.create(ForgeRegistries.MOB_EFFECTS"));
        assertTrue(effects.contains(
                "MOB_EFFECTS.register(\"mana_eruption\""));
        assertTrue(potions.contains(
                "DeferredRegister.create(ForgeRegistries.POTIONS"));
        assertTrue(potions.contains("\"mana_eruption_long\""));
        assertTrue(potions.contains("\"mana_eruption_strong\""));
        assertTrue(potions.contains(
                "DISPLAY_NAME = \"until_eternity.mana_eruption\""));
        assertTrue(potions.contains("LONG_DURATION = 20 * 60 * 10"));
        assertTrue(potions.contains("STRONG_DURATION = 20 * 60 * 5"));
        assertTrue(Pattern.compile("LONG_DURATION,\\s*0\\)")
                .matcher(potions).find());
        assertTrue(Pattern.compile("STRONG_DURATION,\\s*1\\)")
                .matcher(potions).find());
        assertTrue(mod.contains("ModMobEffects.register(modEventBus);"));
        assertTrue(mod.contains("ModPotions.register(modEventBus);"));
    }

    @Test
    void creativeTabContainsOnlyTheTwoDrinkablePotionStacks()
            throws IOException {
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "item", "ModCreativeModeTabs.java")));

        assertEquals(2, occurrences(source, "PotionUtils.setPotion("));
        assertEquals(2, occurrences(source, "new ItemStack(Items.POTION)"));
        assertEquals(
                1,
                occurrences(source, "ModPotions.MANA_ERUPTION_LONG.get()"));
        assertEquals(
                1,
                occurrences(source, "ModPotions.MANA_ERUPTION_STRONG.get()"));
        assertFalse(source.contains("Items.SPLASH_POTION"));
        assertFalse(source.contains("Items.LINGERING_POTION"));
        assertFalse(source.contains("Items.TIPPED_ARROW"));
    }

    @Test
    void mixinTargetsBothMappedLivingEntityEntryPoints()
            throws IOException {
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "mixin", "ManaEruptionStackingMixin.java")));
        JsonObject config = JsonParser.parseString(Files.readString(
                RESOURCES.resolve("until_eternity.mixins.json")))
                .getAsJsonObject();

        assertTrue(source.contains("@Mixin(LivingEntity.class)"));
        assertTrue(source.contains(
                "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;"));
        assertTrue(source.contains(
                "forceAddEffect(Lnet/minecraft/world/effect/MobEffectInstance;"));
        assertEquals(2, occurrences(source, "at = @At(\"HEAD\")"));
        assertEquals(2, occurrences(source, "cancellable = true"));
        assertEquals(2, occurrences(source, "require = 1"));
        assertFalse(source.contains("remap = false"));
        assertTrue(source.contains("entity.level().isClientSide"));
        assertTrue(source.contains("ManaEruptionMergeGuard.call("));
        assertTrue(source.contains("ManaEruptionMergeGuard.run("));
        assertTrue(config.getAsJsonArray("mixins").asList().stream()
                .anyMatch(element -> "ManaEruptionStackingMixin"
                        .equals(element.getAsString())));
    }

    @Test
    void translationsAndOriginalRgbaIconExist() throws IOException {
        for (String language : new String[]{"en_us.json", "zh_cn.json"}) {
            JsonObject translations = JsonParser.parseString(Files.readString(
                    RESOURCES.resolve(Path.of(
                            "assets", "until_eternity", "lang", language))))
                    .getAsJsonObject();
            assertTrue(translations.has(
                    "effect.until_eternity.mana_eruption"));
            assertTrue(translations.has(
                    "item.minecraft.potion.effect."
                            + "until_eternity.mana_eruption"));
        }

        Path icon = RESOURCES.resolve(Path.of(
                "assets", "until_eternity", "textures",
                "mob_effect", "mana_eruption.png"));
        BufferedImage image = ImageIO.read(icon.toFile());
        assertNotNull(image);
        assertEquals(18, image.getWidth());
        assertEquals(18, image.getHeight());
        assertTrue(image.getColorModel().hasAlpha());
        boolean transparentPixel = false;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) {
                    transparentPixel = true;
                }
            }
        }
        assertTrue(transparentPixel);
        assertTrue(Files.isRegularFile(ROOT.resolve(Path.of(
                "tools", "generate_mana_eruption_icon.py"))));
    }

    @Test
    void goetyIsMandatoryAndNoBrewingRecipeWasAdded()
            throws IOException {
        String modsToml = Files.readString(
                RESOURCES.resolve(Path.of("META-INF", "mods.toml")));
        Pattern goetyDependency = Pattern.compile(
                "\\[\\[dependencies\\.until_eternity\\]\\]\\s*"
                        + "modId=\"goety\"\\s*"
                        + "mandatory=true\\s*"
                        + "versionRange=\"\\[2\\.5\\.46\\.1,\\)\"",
                Pattern.MULTILINE);
        assertTrue(goetyDependency.matcher(modsToml).find());

        Path recipes = RESOURCES.resolve(Path.of(
                "data", "until_eternity", "recipes"));
        try (var files = Files.walk(recipes)) {
            assertTrue(files.filter(Files::isRegularFile)
                    .noneMatch(path -> path.getFileName().toString()
                            .contains("mana_eruption")));
        }
    }

    private static int occurrences(String source, String target) {
        return (source.length()
                - source.replace(target, "").length())
                / target.length();
    }
}
