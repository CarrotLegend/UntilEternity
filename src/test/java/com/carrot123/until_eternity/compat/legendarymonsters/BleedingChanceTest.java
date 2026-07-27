package com.carrot123.until_eternity.compat.legendarymonsters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BleedingChanceTest {
    @Test
    void usesExactHalfOpenFiftyPercentThreshold() {
        assertTrue(BleedingChance.shouldApply(0.0F));
        assertTrue(BleedingChance.shouldApply(Math.nextDown(0.5F)));
        assertFalse(BleedingChance.shouldApply(0.5F));
        assertFalse(BleedingChance.shouldApply(Math.nextDown(1.0F)));
    }
}
