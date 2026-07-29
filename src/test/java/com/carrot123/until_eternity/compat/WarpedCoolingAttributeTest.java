package com.carrot123.until_eternity.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarpedCoolingAttributeTest {
    @Test
    void coolingRingTargetsSpellCooldown() {
        assertEquals(
                "goety_revelation:spell_cooldown",
                GoetyRevelationAttributesCompat.SPELL_COOLDOWN.toString()
        );
    }
}
