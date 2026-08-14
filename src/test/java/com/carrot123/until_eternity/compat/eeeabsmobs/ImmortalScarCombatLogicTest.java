package com.carrot123.until_eternity.compat.eeeabsmobs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImmortalScarCombatLogicTest {
    @Test
    void applicationChanceUsesAnExclusiveFiftyPercentBoundary() {
        assertTrue(ImmortalScarCombatLogic.shouldApply(0.0F));
        assertTrue(ImmortalScarCombatLogic.shouldApply(0.49999997F));
        assertFalse(ImmortalScarCombatLogic.shouldApply(0.5F));
        assertFalse(ImmortalScarCombatLogic.shouldApply(1.0F));
        assertFalse(ImmortalScarCombatLogic.shouldApply(Float.NaN));
    }

    @Test
    void damageIsDoubledAndSafelyBounded() {
        assertEquals(40.0F,
                ImmortalScarCombatLogic.doubleDamage(20.0F));
        assertEquals(Float.MAX_VALUE,
                ImmortalScarCombatLogic.doubleDamage(Float.MAX_VALUE));
        assertEquals(0.0F,
                ImmortalScarCombatLogic.doubleDamage(0.0F));
        assertEquals(-1.0F,
                ImmortalScarCombatLogic.doubleDamage(-1.0F));
    }

    @Test
    void effectParametersRemainLevelOneForTenSeconds() {
        assertEquals(0.5F, ImmortalScarCombatLogic.APPLICATION_CHANCE);
        assertEquals(200, ImmortalScarCombatLogic.DURATION_TICKS);
        assertEquals(0, ImmortalScarCombatLogic.AMPLIFIER);
        assertEquals(2.0F, ImmortalScarCombatLogic.DAMAGE_MULTIPLIER);
    }
}
