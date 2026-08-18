package com.carrot123.until_eternity.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniversalSmithingTemplateResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123", "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of("src", "main", "resources"));
    private static final Path ASSETS = RESOURCES.resolve(Path.of("assets", "until_eternity"));

    @Test
    void registrationUsesDedicatedSmithingTemplateTypeAndCreativeTab() throws IOException {
        String items = source("item/ModItems.java").replaceAll("\\s+", " ");
        String template = source("item/UniversalSmithingTemplateItem.java");
        String creativeTab = source("item/ModCreativeModeTabs.java");

        assertTrue(items.contains(
                "RegistryObject<UniversalSmithingTemplateItem> UNIVERSAL_SMITHING_TEMPLATE = "
                        + "ITEMS.register(\"universal_smithing_template\", "
                        + "UniversalSmithingTemplateItem::new)"));
        assertTrue(template.contains(
                "class UniversalSmithingTemplateItem extends SmithingTemplateItem"));
        assertTrue(template.contains("public String getDescriptionId()"));
        assertTrue(template.contains("return DESCRIPTION_ID"));
        assertTrue(creativeTab.contains(
                "output.accept(ModItems.UNIVERSAL_SMITHING_TEMPLATE.get())"));

        assertFalse(template.contains("Rarity."));
        assertFalse(template.contains("fireResistant()"));
    }

    @Test
    void constructorUsesTranslationsAndVanillaSlotIcons() throws IOException {
        String template = source("item/UniversalSmithingTemplateItem.java");

        assertTrue(template.contains(
                "Component.translatable(APPLIES_TO_ID).withStyle(ChatFormatting.BLUE)"));
        assertTrue(template.contains(
                "Component.translatable(INGREDIENTS_ID).withStyle(ChatFormatting.BLUE)"));
        assertTrue(template.contains(
                "Component.translatable(UPGRADE_ID).withStyle(ChatFormatting.GRAY)"));
        assertTrue(template.contains("Component.translatable(BASE_SLOT_DESCRIPTION_ID)"));
        assertTrue(template.contains("Component.translatable(ADDITIONS_SLOT_DESCRIPTION_ID)"));

        List<String> baseIcons = List.of(
                "item/empty_armor_slot_helmet",
                "item/empty_slot_sword",
                "item/empty_armor_slot_chestplate",
                "item/empty_slot_pickaxe",
                "item/empty_armor_slot_leggings",
                "item/empty_slot_axe",
                "item/empty_armor_slot_boots",
                "item/empty_slot_hoe",
                "item/empty_slot_shovel"
        );
        for (String icon : baseIcons) {
            assertTrue(template.contains("minecraftIcon(\"" + icon + "\")"));
        }
        assertTrue(template.contains("List.of(minecraftIcon(\"item/empty_slot_ingot\"))"));
        assertTrue(template.contains("new ResourceLocation(\"minecraft\", path)"));
    }

    @Test
    void modelTextureAndTranslationsAreComplete() throws IOException {
        JsonObject model = json(ASSETS.resolve(
                Path.of("models", "item", "universal_smithing_template.json")));
        assertEquals("minecraft:item/generated", model.get("parent").getAsString());
        assertEquals("until_eternity:item/universal_smithing_template",
                model.getAsJsonObject("textures").get("layer0").getAsString());

        BufferedImage texture = ImageIO.read(ASSETS.resolve(Path.of(
                "textures", "item", "universal_smithing_template.png")).toFile());
        assertNotNull(texture);
        assertEquals(16, texture.getWidth());
        assertEquals(16, texture.getHeight());
        assertTrue(texture.getColorModel().hasAlpha());
        assertFalse(Files.exists(RESOURCES.resolve(Path.of("assets", "model.png"))));

        JsonObject english = json(ASSETS.resolve(Path.of("lang", "en_us.json")));
        JsonObject chinese = json(ASSETS.resolve(Path.of("lang", "zh_cn.json")));
        assertTranslations(english,
                "Universal Smithing Template", "Universal Upgrade", "Ingot",
                "Universal Upgrade", "Add item to upgrade", "Add ingot");
        assertTranslations(chinese,
                "通用锻造模板", "通用升级", "矿锭",
                "通用升级", "放入要升级的物品", "放入矿锭");
    }

    @Test
    void noRecipeOrSmithingMenuMixinIsAdded() throws IOException {
        Path recipes = RESOURCES.resolve(Path.of("data", "until_eternity", "recipes"));
        try (Stream<Path> paths = Files.walk(recipes)) {
            assertFalse(paths.filter(Files::isRegularFile).anyMatch(path -> {
                try {
                    return Files.readString(path).contains("universal_smithing_template");
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            }));
        }

        String mixins = Files.readString(
                RESOURCES.resolve("until_eternity.mixins.json"));
        assertFalse(mixins.contains("SmithingMenu"));
        assertFalse(Files.exists(JAVA.resolve("mixin/SmithingMenuMixin.java")));
    }

    private static void assertTranslations(
            JsonObject language,
            String name,
            String appliesTo,
            String ingredients,
            String upgrade,
            String baseSlot,
            String additionsSlot
    ) {
        assertEquals(name, value(language,
                "item.until_eternity.universal_smithing_template"));
        assertEquals(appliesTo, value(language,
                "item.until_eternity.smithing_template.universal.applies_to"));
        assertEquals(ingredients, value(language,
                "item.until_eternity.smithing_template.universal.ingredients"));
        assertEquals(upgrade, value(language,
                "item.until_eternity.smithing_template.universal.upgrade"));
        assertEquals(baseSlot, value(language,
                "item.until_eternity.smithing_template.universal.base_slot_description"));
        assertEquals(additionsSlot, value(language,
                "item.until_eternity.smithing_template.universal.additions_slot_description"));
    }

    private static String value(JsonObject language, String key) {
        return language.get(key).getAsString();
    }

    private static String source(String relative) throws IOException {
        return Files.readString(JAVA.resolve(relative));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
