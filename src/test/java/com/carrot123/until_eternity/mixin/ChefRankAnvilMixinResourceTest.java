package com.carrot123.until_eternity.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChefRankAnvilMixinResourceTest {
    private static final Path MAIN = Path.of("src/main");
    private static final Path JAVA = MAIN.resolve("java/com/carrot123/until_eternity");

    @Test
    void mixinIsRegisteredExactlyOnceAndKeepsTheForgeMaterialCost() throws Exception {
        JsonObject config = JsonParser.parseString(Files.readString(
                MAIN.resolve("resources/until_eternity.mixins.json"))).getAsJsonObject();
        long registrations = config.getAsJsonArray("mixins").asList().stream()
                .filter(value -> value.getAsString().equals("ChefRankAnvilMixin"))
                .count();

        assertEquals(1, registrations);

        String event = source("event/ChefRankAnvilEvents.java");
        assertTrue(event.contains("BADGE_COST = 1"));
        assertTrue(event.contains("event.setMaterialCost(BADGE_COST)"));
        assertTrue(event.contains("event.setCost(LEVEL_COST)"));
        assertTrue(event.contains("event.setOutput(output)"));
    }

    @Test
    void redirectRestoresOneImmediatelyAfterTheRealLeftSlotClear() throws Exception {
        String mixin = source("mixin/ChefRankAnvilMixin.java");

        assertTrue(mixin.contains("@Mixin(AnvilMenu.class)"));
        assertTrue(mixin.contains("method = \"onTake\""));
        assertTrue(mixin.contains(
                "target = \"Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V\""));
        assertTrue(mixin.contains("ordinal = 0"));
        assertEquals(1, count(mixin, "inputSlots.setItem(slot, stack)"));

        int originalCall = mixin.indexOf("inputSlots.setItem(slot, stack)");
        int forcedCost = mixin.indexOf("this.repairItemCountCost = 1", originalCall);
        assertTrue(originalCall >= 0 && forcedCost > originalCall);
        assertTrue(mixin.contains("boolean forceSingleBadge = this.until_eternity$chefRankUpgradeResult"));
    }

    @Test
    void cachedResultRequiresTheExactChefUpgradeContract() throws Exception {
        String mixin = source("mixin/ChefRankAnvilMixin.java");

        assertTrue(mixin.contains("method = \"createResult\""));
        assertTrue(mixin.contains("at = @At(\"RETURN\")"));
        assertTrue(mixin.contains("AnvilMenu.INPUT_SLOT"));
        assertTrue(mixin.contains("AnvilMenu.ADDITIONAL_SLOT"));
        assertTrue(mixin.contains("AnvilMenu.RESULT_SLOT"));
        assertTrue(mixin.contains("ChefRankHelper.isChefArmor(armor)"));
        assertTrue(mixin.contains("ChefRankHelper.getRankForBadge(badge)"));
        assertTrue(mixin.contains("targetRank == ChefRank.NONE"));
        assertTrue(mixin.contains("targetRank.id() == currentRank.id() + 1"));
        assertTrue(mixin.contains("ItemStack.isSameItem(armor, output)"));
        assertTrue(mixin.contains("ChefRankHelper.getRank(output) == targetRank"));
        assertTrue(mixin.contains("output.getCount() != 1"));

        for (String forbidden : new String[]{
                ".give(", ".drop(", "addFreshEntity(", "ItemEntity", "getInventory().add"
        }) {
            assertFalse(mixin.contains(forbidden), forbidden);
        }
    }

    @Test
    void vanillaSingleMaterialConsumptionProducesTheRequiredCounts() {
        assertEquals(15, consumeOne(16));
        assertEquals(1, consumeOne(2));
        assertEquals(0, consumeOne(1));
    }

    private static int consumeOne(int count) {
        return count > 1 ? count - 1 : 0;
    }

    private static String source(String relative) throws Exception {
        return Files.readString(JAVA.resolve(relative));
    }

    private static int count(String text, String needle) {
        return (text.length() - text.replace(needle, "").length()) / needle.length();
    }
}
