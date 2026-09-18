package com.carrot123.until_eternity.item.curio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CurioEquipmentHelperTest {
    @Test
    void excludesOnlyTheExactCurrentSlot() {
        assertTrue(CurioEquipmentHelper.isExcludedSlot(
                "belt", 0, "belt", 0));
        assertFalse(CurioEquipmentHelper.isExcludedSlot(
                "belt", 1, "belt", 0));
        assertFalse(CurioEquipmentHelper.isExcludedSlot(
                "ring", 0, "belt", 0));
    }

    @Test
    void missingExclusionCountsEverySlot() {
        assertFalse(CurioEquipmentHelper.isExcludedSlot(
                "belt", 0, null, -1));
    }
}
