package com.carrot123.until_eternity.item.curio;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DyingFuryItemTest {
    @Test
    void sourceDeclaresTheRequestedBaseProperties() throws IOException {
        String source = Files.readString(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "item", "curio",
                "DyingFuryItem.java"));

        assertTrue(source.contains(
                "extends BaseModCurioItem"));
        assertTrue(source.contains(".rarity(Rarity.EPIC)"));
        assertTrue(source.contains(".fireResistant()"));
        assertFalse(source.contains("getAttributeModifiers"));
        assertFalse(source.contains("canEquip"));
        assertTrue(source.contains("List.of()"));
    }

    @Test
    void equippedCheckUsesAnyFunctionalCuriosSlot() throws IOException {
        String source = Files.readString(Path.of(
                "src", "main", "java", "com", "carrot123",
                "until_eternity", "event",
                "DyingFuryCombatEvents.java"));

        assertTrue(source.contains(
                "handler.isEquipped(ModItems.DYING_FURY.get())"));
        assertFalse(source.contains("slotContext().identifier()"));
        assertFalse(source.contains("ModCurioSlots"));
    }
}
