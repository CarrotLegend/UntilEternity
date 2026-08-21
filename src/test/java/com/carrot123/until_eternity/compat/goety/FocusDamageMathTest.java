package com.carrot123.until_eternity.compat.goety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FocusDamageMathTest {
    @Test
    void finalAttributeValuesAreDamageMultipliers() {
        assertEquals(40.0F, FocusDamageMath.apply(40.0F, 1.0D));
        assertEquals(48.0F, FocusDamageMath.apply(40.0F, 1.20D));
        assertEquals(58.0F, FocusDamageMath.apply(40.0F, 1.45D));
        assertEquals(60.0F, FocusDamageMath.apply(40.0F, 1.50D));
        assertEquals(150.0F, FocusDamageMath.apply(
                100.0F, 1.0D + 0.15D + 0.10D + 0.25D));
    }

    @Test
    void invalidInputsAndOverflowKeepOriginalDamage() {
        assertEquals(0.0F, FocusDamageMath.apply(0.0F, 1.5D));
        assertEquals(-1.0F, FocusDamageMath.apply(-1.0F, 1.5D));
        assertEquals(10.0F, FocusDamageMath.apply(10.0F, 0.0D));
        assertEquals(10.0F, FocusDamageMath.apply(10.0F, -1.0D));
        assertEquals(10.0F,
                FocusDamageMath.apply(10.0F, Double.NaN));
        assertEquals(10.0F,
                FocusDamageMath.apply(10.0F,
                        Double.POSITIVE_INFINITY));
        assertEquals(Float.MAX_VALUE,
                FocusDamageMath.apply(Float.MAX_VALUE, 2.0D));
    }
}
