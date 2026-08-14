package com.carrot123.until_eternity.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffAffixResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));
    private static final Path RESOURCES = ROOT.resolve(Path.of(
            "src", "main", "resources"));

    @Test
    void rerollRecipeIsStrictAndCopiesTheCompleteStaff() throws IOException {
        String source = source("recipe/StaffAffixRerollRecipe.java");
        assertTrue(source.contains("nonEmpty == 2"));
        assertTrue(source.contains("ModItems.SPAWNER_FRAGMENT.get()"));
        assertTrue(source.contains("staff.copy()"));
        assertTrue(source.contains("result.setCount(1)"));
        assertFalse(source.contains("new ItemStack(staff.getItem())"));
        assertFalse(source.contains("StaffAffixHelper.roll"));

        JsonObject recipe = JsonParser.parseString(Files.readString(
                RESOURCES.resolve(Path.of("data", "until_eternity",
                        "recipes", "staff_affix_reroll.json"))))
                .getAsJsonObject();
        assertEquals("until_eternity:staff_affix_reroll",
                recipe.get("type").getAsString());
        String serializers = source("recipe/ModRecipeSerializers.java");
        assertTrue(serializers.contains("\"staff_affix_reroll\""));
        assertTrue(serializers.contains("StaffAffixRerollRecipe::new"));
    }

    @Test
    void craftingEventSeparatesNormalCraftUpgradeAndReroll() throws IOException {
        String source = source("event/StaffAffixEvents.java");
        assertTrue(source.contains("PlayerEvent.ItemCraftedEvent"));
        assertTrue(source.contains("level().isClientSide"));
        assertTrue(source.contains("STAFF_AFFIX_REROLL.get()"));
        assertTrue(source.contains("STAFF_UPGRADE.get()"));
        assertTrue(source.contains("containsUpgradeableStaff"));
        assertTrue(source.contains("StaffAffixHelper.roll"));
        assertFalse(source.contains("PlayerTickEvent"));
        assertFalse(source.contains("PlayerLoggedInEvent"));
    }

    @Test
    void namesAttributesTranslationsAndOptionalJeiAreComplete() throws IOException {
        String helper = source(
                "compat/ironsspellbooks/StaffAffixHelper.java");
        assertTrue(helper.contains(
                "random.nextInt(values.length)"));
        assertTrue(helper.contains("Tag.TAG_STRING"));
        assertTrue(helper.contains(
                "until_eternity:staff_affix"));

        String mixin = source("mixin/ItemStackHoverNameMixin.java");
        assertTrue(mixin.contains("StaffAffixHelper.composeHoverName"));
        assertFalse(mixin.contains("Component.literal(\" +\""));
        assertFalse(mixin.contains("setHoverName"));

        String attributes = source("event/StaffUpgradeAttributeHandler.java");
        assertTrue(attributes.contains("EquipmentSlot.MAINHAND"));
        assertEquals(1, occurrences(attributes,
                "AttributeModifier.Operation.MULTIPLY_BASE"));
        assertTrue(attributes.contains("StaffUpgradeModifierIds"));
        assertTrue(attributes.contains("StaffAffixModifierIds"));

        JsonObject english = json("en_us.json");
        JsonObject chinese = json("zh_cn.json");
        for (String key : new String[]{"ancient", "newborn", "decadent",
                "refined", "noble", "modest", "divine"}) {
            assertTrue(english.has("affix.until_eternity." + key));
            assertTrue(chinese.has("affix.until_eternity." + key));
        }
        for (String key : new String[]{"common", "excellent", "rare",
                "epic", "legendary"}) {
            assertTrue(english.has("staff_upgrade.until_eternity." + key));
            assertTrue(chinese.has("staff_upgrade.until_eternity." + key));
        }
        String gradle = Files.readString(ROOT.resolve("build.gradle"));
        assertTrue(gradle.contains("compileOnly fg.deobf(\"mezz.jei:"));
        assertFalse(gradle.contains("implementation fg.deobf(\"mezz.jei:"));
        assertTrue(source("compat/jei/UntilEternityJeiPlugin.java")
                .contains("@JeiPlugin"));
    }

    private static JsonObject json(String name) throws IOException {
        return JsonParser.parseString(Files.readString(RESOURCES.resolve(
                Path.of("assets", "until_eternity", "lang", name))))
                .getAsJsonObject();
    }

    private static String source(String path) throws IOException {
        return Files.readString(JAVA.resolve(path));
    }

    private static int occurrences(String source, String value) {
        return (source.length() - source.replace(value, "").length())
                / value.length();
    }
}
