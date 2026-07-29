package com.carrot123.until_eternity.item.curio;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarpedRingStackingTest {
    private static final Path JAVA = Path.of(
            "src", "main", "java", "com", "carrot123",
            "until_eternity");

    @Test
    void registrationsRestoreThePreviousTwoAndThreeRingLimits()
            throws IOException {
        String items = Files.readString(JAVA.resolve("item/ModItems.java"));

        assertTrue(items.contains(
                "4.0, AttributeModifier.Operation.ADDITION, 3)"));
        assertTrue(items.contains(
                "1.0, AttributeModifier.Operation.ADDITION, 2)"));
        assertTrue(items.contains(
                "0.08, AttributeModifier.Operation.MULTIPLY_BASE, 3)"));
        assertTrue(items.contains(
                "0.05, AttributeModifier.Operation.MULTIPLY_BASE, 3)"));
        assertTrue(items.contains(
                "0.05, AttributeModifier.Operation.ADDITION, 3)"));
        assertTrue(items.contains(
                "SPELL_COOLDOWN,\n                    \"spell_cooldown\",\n"
                        + "                    0.05, "
                        + "AttributeModifier.Operation.ADDITION, 3)"));
    }

    @Test
    void ordinaryRingsCountAllFunctionalCurioSlotsWithoutSlotNameGuard()
            throws IOException {
        String ring = Files.readString(
                JAVA.resolve("item/curio/WarpedRingItem.java"));
        String helper = Files.readString(
                JAVA.resolve("item/curio/CurioEquipmentHelper.java"));

        assertTrue(ring.contains("countEquipped("));
        assertTrue(ring.contains("< maxEquipped"));
        assertFalse(ring.contains("identifier()"));
        assertTrue(helper.contains("handler.getCurios().values()"));
        assertTrue(helper.contains("stacksHandler.getStacks()"));
        assertFalse(helper.contains("getCosmeticStacks()"));
        assertFalse(helper.contains("findFirstCurio"));
        assertFalse(helper.contains("isEquipped"));
    }

    @Test
    void voidRingRemainsLimitedToOne()
            throws IOException {
        String ring = Files.readString(
                JAVA.resolve("item/curio/VoidRingItem.java"));

        assertTrue(ring.contains("MAX_EQUIPPED = 1"));
        assertTrue(ring.contains("< MAX_EQUIPPED"));
    }
}
