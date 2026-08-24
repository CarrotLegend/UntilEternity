package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.combat.CookingFrenzyProgression;
import com.carrot123.until_eternity.combat.ForcedHitDamageMath;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import net.minecraft.nbt.CompoundTag;

class TrueChefsKnifeTest {
    @Test
    void frenzyProgressionStartsAtOneAndCapsAtTen() {
        assertEquals(0, CookingFrenzyProgression.nextAmplifier(-1));
        assertEquals(1, CookingFrenzyProgression.nextAmplifier(0));
        assertEquals(9, CookingFrenzyProgression.nextAmplifier(8));
        assertEquals(9, CookingFrenzyProgression.nextAmplifier(9));
        assertEquals(9, CookingFrenzyProgression.nextAmplifier(100));
        assertEquals(200, CookingFrenzyProgression.DURATION_TICKS);
    }

    @Test
    void forcedHitKeepsPositiveFiniteHooksAndRejectsInvalidResults() {
        assertEquals(7.5F, ForcedHitDamageMath.preservePositiveHookResult(5.0F, 7.5F));
        assertEquals(5.0F, ForcedHitDamageMath.preservePositiveHookResult(5.0F, 0.0F));
        assertEquals(5.0F, ForcedHitDamageMath.preservePositiveHookResult(5.0F, -1.0F));
        assertEquals(5.0F, ForcedHitDamageMath.preservePositiveHookResult(5.0F, Float.NaN));
        assertEquals(5.0F, ForcedHitDamageMath.preservePositiveHookResult(
                5.0F, Float.POSITIVE_INFINITY));
    }

    @Test
    void unbreakableRepairPreservesExistingStackData() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Damage", 123);
        tag.putString("ChefRank", "master");
        CompoundTag nested = new CompoundTag();
        nested.putInt("Value", 7);
        tag.put("Custom", nested);

        UnbreakableStackData.apply(tag);

        assertTrue(tag.getBoolean("Unbreakable"));
        assertEquals(123, tag.getInt("Damage"));
        assertEquals("master", tag.getString("ChefRank"));
        assertEquals(7, tag.getCompound("Custom").getInt("Value"));
    }

    @Test
    void everyRequiredStackEntryPointRepairsBeforeVanillaDurabilityLogic() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/carrot123/until_eternity/item/TrueChefsKnifeItem.java"));
        assertTrue(source.contains("ItemStack getDefaultInstance()"));
        assertTrue(source.contains("void inventoryTick("));
        assertTrue(source.contains("void onCraftedBy("));
        assertTrue(source.contains("boolean hurtEnemy("));
        assertTrue(source.contains("boolean mineBlock("));
        assertTrue(source.indexOf("ensureUnbreakable(stack);\n        boolean result = super.hurtEnemy") >= 0);
        assertTrue(source.indexOf("ensureUnbreakable(stack);\n        return super.mineBlock") >= 0);
        assertTrue(source.contains("stack.getOrCreateTag()"));
        String dataHelper = Files.readString(Path.of(
                "src/main/java/com/carrot123/until_eternity/item/UnbreakableStackData.java"));
        assertTrue(dataHelper.contains("putBoolean(\"Unbreakable\", true)"));
    }

    @Test
    void unavoidableTooltipUsesOneRedTranslatedLineAndKeepsVanillaTooltip() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/carrot123/until_eternity/item/TrueChefsKnifeItem.java"));
        String key = "tooltip.until_eternity.true_chefs_knife.unavoidable";

        assertTrue(source.contains("void appendHoverText("));
        assertTrue(source.contains("super.appendHoverText(stack, level, tooltip, flag);"));
        assertEquals(1, occurrences(source, "Component.translatable(\n                \"" + key + "\""));
        assertTrue(source.contains("withStyle(ChatFormatting.RED)"));
        assertTrue(!source.contains("§c"));

        JsonObject zhCn = language("zh_cn");
        JsonObject enUs = language("en_us");
        assertEquals("没人能躲开你的攻击", zhCn.get(key).getAsString());
        assertEquals("No one can evade your attacks", enUs.get(key).getAsString());
    }

    private static JsonObject language(String locale) throws Exception {
        return JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/until_eternity/lang/" + locale + ".json"))).getAsJsonObject();
    }

    private static int occurrences(String text, String needle) {
        return (text.length() - text.replace(needle, "").length()) / needle.length();
    }
}
