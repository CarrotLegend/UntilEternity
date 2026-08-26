package com.carrot123.until_eternity.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManaEruptionStackingTest {
    @Test
    void amplifierIsAlwaysLimitedToLevelsOneThroughTen() {
        assertEquals(0, ManaEruptionStacking.clampAmplifier(-100));
        assertEquals(0, ManaEruptionStacking.clampAmplifier(0));
        assertEquals(9, ManaEruptionStacking.clampAmplifier(9));
        assertEquals(9, ManaEruptionStacking.clampAmplifier(20));
    }

    @Test
    void repeatedApplicationsFollowTheSpecifiedLevelFormula() {
        assertEquals(1, ManaEruptionStacking.mergeAmplifier(0, 0));
        assertEquals(1, ManaEruptionStacking.mergeAmplifier(0, 1));
        assertEquals(2, ManaEruptionStacking.mergeAmplifier(1, 0));
        assertEquals(5, ManaEruptionStacking.mergeAmplifier(4, 1));
        assertEquals(9, ManaEruptionStacking.mergeAmplifier(8, 0));
        assertEquals(9, ManaEruptionStacking.mergeAmplifier(9, 0));
        assertEquals(9, ManaEruptionStacking.mergeAmplifier(20, 20));
    }

    @Test
    void durationNeverBecomesShorter() {
        assertEquals(12000, ManaEruptionStacking.mergeDuration(200, 12000));
        assertEquals(6000, ManaEruptionStacking.mergeDuration(200, 6000));
        assertEquals(9600, ManaEruptionStacking.mergeDuration(9600, 6000));
    }

    @Test
    void ironsSpellPowerScalingProducesTenPercentPerLevel() {
        assertEquals(0.10D, ManaEruptionStacking.attributeAmount(0), 1.0E-12D);
        assertEquals(0.20D, ManaEruptionStacking.attributeAmount(1), 1.0E-12D);
        assertEquals(0.50D, ManaEruptionStacking.attributeAmount(4), 1.0E-12D);
        assertEquals(1.00D, ManaEruptionStacking.attributeAmount(9), 1.0E-12D);
        assertEquals(1.00D, ManaEruptionStacking.attributeAmount(99), 1.0E-12D);
    }

    @Test
    void focusDamageScalingProducesTenPercentPerLevel() {
        assertEquals(0.10D, ManaEruptionStacking.focusDamageAmount(0), 1.0E-12D);
        assertEquals(0.20D, ManaEruptionStacking.focusDamageAmount(1), 1.0E-12D);
        assertEquals(0.30D, ManaEruptionStacking.focusDamageAmount(2), 1.0E-12D);
        assertEquals(1.00D, ManaEruptionStacking.focusDamageAmount(9), 1.0E-12D);
        assertEquals(1.00D, ManaEruptionStacking.focusDamageAmount(99), 1.0E-12D);
    }
}
