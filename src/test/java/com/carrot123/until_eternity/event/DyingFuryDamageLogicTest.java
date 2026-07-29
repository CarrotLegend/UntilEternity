package com.carrot123.until_eternity.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DyingFuryDamageLogicTest {
    private static final double EPSILON = 0.000001D;

    @Test
    void requestedHealthSnapshotsProduceExactMultipliers() {
        assertEquals(1.0D,
                DyingFuryDamageLogic.calculateMultiplier(20.0F, 20.0F),
                EPSILON);
        assertEquals(1.002D,
                DyingFuryDamageLogic.calculateMultiplier(20.0F, 19.0F),
                EPSILON);
        assertEquals(1.02D,
                DyingFuryDamageLogic.calculateMultiplier(20.0F, 10.0F),
                EPSILON);
        assertEquals(1.10D,
                DyingFuryDamageLogic.calculateMultiplier(100.0F, 50.0F),
                EPSILON);
    }

    @Test
    void damageUsesTheCurrentEventAmount() {
        assertEquals(100.0F,
                DyingFuryDamageLogic.enhanceDamage(100.0F, 20.0F, 20.0F),
                0.0001F);
        assertEquals(100.2F,
                DyingFuryDamageLogic.enhanceDamage(100.0F, 20.0F, 19.0F),
                0.0001F);
        assertEquals(102.0F,
                DyingFuryDamageLogic.enhanceDamage(100.0F, 20.0F, 10.0F),
                0.0001F);
        assertEquals(110.0F,
                DyingFuryDamageLogic.enhanceDamage(100.0F, 100.0F, 50.0F),
                0.0001F);
    }

    @Test
    void healthIsClampedWithoutUsingAbsorption() {
        assertEquals(0.0D,
                DyingFuryDamageLogic.calculateLostHealth(20.0F, 25.0F),
                EPSILON);
        assertEquals(20.0D,
                DyingFuryDamageLogic.calculateLostHealth(20.0F, -5.0F),
                EPSILON);
    }

    @Test
    void invalidHealthInputsDoNotCreateABonus() {
        assertEquals(1.0D,
                DyingFuryDamageLogic.calculateMultiplier(Float.NaN, 10.0F),
                EPSILON);
        assertEquals(1.0D,
                DyingFuryDamageLogic.calculateMultiplier(20.0F, Float.NaN),
                EPSILON);
        assertEquals(1.0D,
                DyingFuryDamageLogic.calculateMultiplier(
                        Float.POSITIVE_INFINITY, 10.0F),
                EPSILON);
        assertEquals(1.0D,
                DyingFuryDamageLogic.calculateMultiplier(0.0F, 0.0F),
                EPSILON);
    }

    @Test
    void nonPositiveDamageIsUnchangedAndOverflowIsClamped() {
        assertEquals(0.0F,
                DyingFuryDamageLogic.enhanceDamage(0.0F, 20.0F, 1.0F));
        assertEquals(-1.0F,
                DyingFuryDamageLogic.enhanceDamage(-1.0F, 20.0F, 1.0F));
        assertTrue(Float.isNaN(DyingFuryDamageLogic.enhanceDamage(
                Float.NaN, 20.0F, 1.0F)));
        assertEquals(Float.MAX_VALUE,
                DyingFuryDamageLogic.enhanceDamage(
                        Float.MAX_VALUE,
                        Float.MAX_VALUE,
                        0.0F));
    }
}
