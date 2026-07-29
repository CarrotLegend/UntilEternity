package com.carrot123.until_eternity.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VoidRingAttributeIdsTest {
    @Test
    void usesTheRealRevelationFixAttributeIds() {
        assertEquals(
                "goety_revelation:spell_power",
                GoetyRevelationAttributesCompat.SPELL_POWER.toString());
        assertEquals(
                "goety_revelation:spell_power_multiplier",
                GoetyRevelationAttributesCompat.SPELL_POWER_MULTIPLIER.toString());
    }
}
