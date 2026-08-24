package com.carrot123.until_eternity.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StaffUpgradeResourceTest {
    private static final Path ROOT = Path.of(".");
    private static final Path MAIN_JAVA = ROOT.resolve(Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity"));

    @Test
    void specialRecipeResourceUsesTheRegisteredSerializer()
            throws IOException {
        Path recipe = ROOT.resolve(Path.of(
                "src", "main", "resources", "data",
                "until_eternity", "recipes", "staff_upgrade.json"));
        JsonObject json = JsonParser.parseString(
                Files.readString(recipe)).getAsJsonObject();
        assertEquals(
                "until_eternity:staff_upgrade",
                json.get("type").getAsString());

        String serializers = Files.readString(MAIN_JAVA.resolve(Path.of(
                "recipe", "ModRecipeSerializers.java")));
        String mod = Files.readString(MAIN_JAVA.resolve(
                "until_eternity.java"));
        assertTrue(serializers.contains(
                "DeferredRegister<RecipeSerializer<?>>"));
        assertTrue(serializers.contains("\"staff_upgrade\""));
        assertTrue(serializers.contains(
                "new SimpleCraftingRecipeSerializer<>("));
        assertTrue(serializers.contains("StaffUpgradeRecipe::new"));
        assertTrue(mod.contains(
                "ModRecipeSerializers.register(modEventBus);"));
    }

    @Test
    void recipeImplementsStrictFiveStepCopyingContract()
            throws IOException {
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "recipe", "StaffUpgradeRecipe.java")));

        assertTrue(source.contains("extends CustomRecipe"));
        assertTrue(source.contains("nonEmptyStacks != 3"));
        assertTrue(source.contains("staffSlot >= 0"));
        assertTrue(source.contains("width * height >= 3"));
        assertTrue(source.contains("ItemRegistry.INK_COMMON.get()"));
        assertTrue(source.contains("ItemRegistry.INK_UNCOMMON.get()"));
        assertTrue(source.contains("ItemRegistry.INK_RARE.get()"));
        assertTrue(source.contains("ItemRegistry.INK_EPIC.get()"));
        assertTrue(source.contains("ItemRegistry.INK_LEGENDARY.get()"));
        assertTrue(source.contains("case 1 -> Items.COAL;"));
        assertTrue(source.contains("case 2 -> Items.LAPIS_LAZULI;"));
        assertTrue(source.contains("case 3 -> Items.IRON_INGOT;"));
        assertTrue(source.contains("case 4 -> Items.DIAMOND;"));
        assertTrue(source.contains("case 5 -> Items.NETHERITE_INGOT;"));
        assertTrue(source.contains("match.staff().copy()"));
        assertTrue(source.contains("result.setCount(1)"));
        assertTrue(source.contains(
                "StaffUpgradeHelper.LEVEL_TAG"));
        assertFalse(source.contains("Items.CHARCOAL"));
        assertFalse(source.contains("ItemTags.COALS"));
        assertFalse(source.contains("getRemainingItems("));
    }

    @Test
    void nameMixinUsesMappedVanillaDescriptorAndDynamicCopy()
            throws IOException {
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "mixin", "ItemStackHoverNameMixin.java")));

        assertTrue(source.contains("@Mixin(ItemStack.class)"));
        assertTrue(source.contains(
                "getHoverName()Lnet/minecraft/network/chat/Component;"));
        assertTrue(source.contains(
                "m_41786_()Lnet/minecraft/network/chat/Component;"));
        assertTrue(source.contains("at = @At(\"RETURN\")"));
        assertTrue(source.contains("cancellable = true"));
        assertTrue(source.contains("require = 1"));
        assertTrue(source.contains("callback.getReturnValue()"));
        assertTrue(source.contains(".copy()"));
        assertTrue(source.contains(
                "StaffAffixHelper.composeHoverName"));
        assertFalse(source.contains(
                "Component.literal(\" +\" + level)"));
        assertTrue(source.contains("remap = false"));
        assertFalse(source.contains("display.Name"));
    }

    @Test
    void attributeHandlerOnlyAddsThreeMainhandModifiers()
            throws IOException {
        String source = Files.readString(MAIN_JAVA.resolve(Path.of(
                "event", "StaffUpgradeAttributeHandler.java")));

        assertTrue(source.contains(
                "event.getSlotType() != EquipmentSlot.MAINHAND"));
        assertTrue(source.contains("AttributeRegistry.SPELL_POWER.get()"));
        assertTrue(source.contains(
                "AttributeRegistry.COOLDOWN_REDUCTION.get()"));
        assertTrue(source.contains(
                "AttributeRegistry.CAST_TIME_REDUCTION.get()"));
        assertEquals(
                3,
                occurrences(
                        source,
                        "AttributeModifier.Operation.ADDITION"));
        assertFalse(source.contains("clearModifiers("));
        assertFalse(source.contains("randomUUID("));
        assertFalse(source.contains("PlayerTickEvent"));
    }

    private static int occurrences(String source, String target) {
        return (source.length()
                - source.replace(target, "").length())
                / target.length();
    }
}
