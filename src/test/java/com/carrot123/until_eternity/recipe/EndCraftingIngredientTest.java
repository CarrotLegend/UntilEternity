package com.carrot123.until_eternity.recipe;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndCraftingIngredientTest {
    @Test
    void compoundsAreRecursiveSubsetsAndIntegralNumbersCrossTypes() {
        CompoundTag expected = new CompoundTag();
        CompoundTag expectedNested = new CompoundTag();
        expectedNested.put("Enabled", ByteTag.ONE);
        expected.put("Nested", expectedNested);

        CompoundTag actual = new CompoundTag();
        CompoundTag actualNested = new CompoundTag();
        actualNested.put("Enabled", IntTag.valueOf(1));
        actualNested.putString("Extra", "kept");
        actual.put("Nested", actualNested);
        actual.putInt("Other", 9);

        assertTrue(EndCraftingIngredient.containsSubset(expected, actual));
        actualNested.putInt("Enabled", 0);
        assertFalse(EndCraftingIngredient.containsSubset(expected, actual));
    }

    @Test
    void listsRemainExact() {
        ListTag expected = new ListTag();
        expected.add(StringTag.valueOf("a"));
        ListTag actual = expected.copy();
        assertTrue(EndCraftingIngredient.containsSubset(expected, actual));
        actual.add(StringTag.valueOf("b"));
        assertFalse(EndCraftingIngredient.containsSubset(expected, actual));
    }
}
