package com.carrot123.until_eternity.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbsoluteDamageMathTest {
    @Test
    void splitsOrdinaryDamageInHalf() {
        assertSplit(40.0F, 100.0F, true, 20.0F, 20.0F);
    }

    @Test
    void leavesTheFinalHitPointForTheDamageSource() {
        assertSplit(30.0F, 8.0F, true, 7.0F, 23.0F);
        assertSplit(20.0F, 6.0F, true, 5.0F, 15.0F);
        assertSplit(20.0F, 1.0F, true, 0.0F, 20.0F);
        assertSplit(1000.0F, 100.0F, true, 99.0F, 901.0F);
    }

    @Test
    void doesNotDirectlyDamageDeadOrInvalidInputs() {
        assertSplit(20.0F, 100.0F, false, 0.0F, 20.0F);
        assertSplit(0.0F, 100.0F, true, 0.0F, 0.0F);
        AbsoluteDamageMath.Split infinite = AbsoluteDamageMath.split(
                Float.POSITIVE_INFINITY, 100.0F, true);
        assertEquals(0.0F, infinite.directDamage());
        assertEquals(Float.POSITIVE_INFINITY, infinite.eventDamage());
        AbsoluteDamageMath.Split nan = AbsoluteDamageMath.split(
                Float.NaN, 100.0F, true);
        assertEquals(0.0F, nan.directDamage());
        assertTrue(Float.isNaN(nan.eventDamage()));
    }

    private static void assertSplit(
            float damage,
            float health,
            boolean alive,
            float expectedDirect,
            float expectedEvent
    ) {
        AbsoluteDamageMath.Split split = AbsoluteDamageMath.split(damage, health, alive);
        assertEquals(expectedDirect, split.directDamage());
        assertEquals(expectedEvent, split.eventDamage());
        assertEquals(damage, split.directDamage() + split.eventDamage());
        assertTrue(split.directDamage() >= 0.0F);
        assertTrue(split.directDamage() <= damage * 0.5F);
        if (split.directDamage() > 0.0F) {
            assertTrue(health - split.directDamage() >= 1.0F);
        }
    }
}
