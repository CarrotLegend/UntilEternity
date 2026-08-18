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
    void vanillaEffectScalingProducesOnePointPerLevel() {
        assertEquals(1.0D, ManaEruptionStacking.attributeAmount(0));
        assertEquals(2.0D, ManaEruptionStacking.attributeAmount(1));
        assertEquals(5.0D, ManaEruptionStacking.attributeAmount(4));
        assertEquals(10.0D, ManaEruptionStacking.attributeAmount(9));
        assertEquals(10.0D, ManaEruptionStacking.attributeAmount(99));
    }
}
