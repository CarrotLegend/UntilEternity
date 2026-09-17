package com.carrot123.until_eternity.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ArmorRendDamageLogicTest {
    @Test
    void requiresMoreThanFiveArmor() {
        assertEquals(10.0F, ArmorRendDamageLogic.apply(10.0F, 5, 5));
    }

    @Test
    void usesActualUncappedNbtLevel() {
        assertEquals(11.0F, ArmorRendDamageLogic.apply(10.0F, 6, 1), 1.0E-6F);
        assertEquals(15.0F, ArmorRendDamageLogic.apply(10.0F, 6, 5), 1.0E-6F);
        assertEquals(20.0F, ArmorRendDamageLogic.apply(10.0F, 6, 10), 1.0E-6F);
    }

    @Test
    void leavesInvalidDamageAlone() {
        assertEquals(0.0F, ArmorRendDamageLogic.apply(0.0F, 20, 5));
        assertEquals(-1.0F, ArmorRendDamageLogic.apply(-1.0F, 20, 5));
        assertEquals(Float.NaN, ArmorRendDamageLogic.apply(Float.NaN, 20, 5));
        assertEquals(Float.POSITIVE_INFINITY,
                ArmorRendDamageLogic.apply(Float.POSITIVE_INFINITY, 20, 5));
    }
}
