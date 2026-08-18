package com.carrot123.until_eternity.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NaturalHealingMathTest {
    @Test
    void natureHealUsesOnePointAsOnePercent() {
        assertEquals(1.0F, NaturalHealingMath.apply(1.0F, 0.0D));
        assertEquals(1.2F, NaturalHealingMath.apply(1.0F, 20.0D));
        assertEquals(1.5F, NaturalHealingMath.apply(1.0F, 50.0D));
        assertEquals(2.0F, NaturalHealingMath.apply(1.0F, 100.0D));
    }

    @Test
    void natureHealAndPuffishHealingStackMultiplicatively() {
        float natural = NaturalHealingMath.apply(1.0F, 20.0D);
        assertEquals(1.8F, natural * 1.5F, 1.0E-6F);
    }

    @Test
    void fastNaturalRegenerationAmountUsesTheSameMultiplier() {
        assertEquals(1.2F,
                NaturalHealingMath.apply(6.0F / 6.0F, 20.0D));
        assertEquals(0.4F,
                NaturalHealingMath.apply(2.0F / 6.0F, 20.0D),
                1.0E-6F);
    }
}
