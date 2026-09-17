package com.carrot123.until_eternity.compat.enigmaticlegacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CursedScrollAttackSpeedMathTest {
    @Test
    void multipliesConfiguredModifierByCurseCount() {
        assertEquals(0.07D, CursedScrollAttackSpeedMath.calculate(0.01D, 7), 1.0E-12D);
        assertEquals(0.35D, CursedScrollAttackSpeedMath.calculate(0.05D, 7), 1.0E-12D);
    }

    @Test
    void rejectsZeroNegativeAndNonFiniteInputs() {
        assertEquals(0.0D, CursedScrollAttackSpeedMath.calculate(0.0D, 7));
        assertEquals(0.0D, CursedScrollAttackSpeedMath.calculate(-0.01D, 7));
        assertEquals(0.0D, CursedScrollAttackSpeedMath.calculate(0.01D, 0));
        assertEquals(0.0D, CursedScrollAttackSpeedMath.calculate(Double.NaN, 7));
        assertEquals(0.0D, CursedScrollAttackSpeedMath.calculate(Double.POSITIVE_INFINITY, 7));
        assertEquals(0.0D, CursedScrollAttackSpeedMath.calculate(Double.MAX_VALUE, 2));
    }
}
