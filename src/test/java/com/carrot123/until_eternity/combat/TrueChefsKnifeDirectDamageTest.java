package com.carrot123.until_eternity.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrueChefsKnifeDirectDamageTest {
    @Test
    void preserveIncreaseAcceptsOnlyFiniteIncreases() {
        assertEquals(7.5F, TrueChefsKnifeDirectDamage.preserveIncrease(5.0F, 7.5F));
        assertEquals(5.0F, TrueChefsKnifeDirectDamage.preserveIncrease(5.0F, 2.0F));
        assertEquals(5.0F, TrueChefsKnifeDirectDamage.preserveIncrease(5.0F, 0.0F));
        assertEquals(5.0F, TrueChefsKnifeDirectDamage.preserveIncrease(5.0F, -1.0F));
        assertEquals(5.0F, TrueChefsKnifeDirectDamage.preserveIncrease(5.0F, Float.NaN));
        assertEquals(5.0F, TrueChefsKnifeDirectDamage.preserveIncrease(
                5.0F, Float.POSITIVE_INFINITY));
        assertEquals(5.0F, TrueChefsKnifeDirectDamage.preserveIncrease(
                5.0F, Float.NEGATIVE_INFINITY));
    }
}
