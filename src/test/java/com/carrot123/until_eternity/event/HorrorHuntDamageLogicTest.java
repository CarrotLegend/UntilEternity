package com.carrot123.until_eternity.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HorrorHuntDamageLogicTest {
    @Test
    void amplifiesDamageByExactlyTen() {
        assertEquals(37.5F,
                HorrorHuntDamageLogic.amplifiedDamage(3.75F));
        assertTrue(Float.isInfinite(
                HorrorHuntDamageLogic.amplifiedDamage(Float.MAX_VALUE)));
    }

    @Test
    void cooldownBecomesReadyOnTheExactBoundary() {
        long next = HorrorHuntDamageLogic.nextProcGameTime(1_000L);
        assertEquals(1_200L, next);
        assertFalse(HorrorHuntDamageLogic.isReady(1_199L, next));
        assertTrue(HorrorHuntDamageLogic.isReady(1_200L, next));
    }
}
