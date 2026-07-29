package com.carrot123.until_eternity.enchantment;

import com.carrot123.until_eternity.compat.ironsspellbooks.PlenitudeManaCost;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlenitudeManaCostTest {
    @Test
    void appliesAllStoredLevelsWithoutRuntimeCap() {
        assertEquals(95, PlenitudeManaCost.calculate(100, 1));
        assertEquals(90, PlenitudeManaCost.calculate(100, 2));
        assertEquals(85, PlenitudeManaCost.calculate(100, 3));
        assertEquals(80, PlenitudeManaCost.calculate(100, 4));
        assertEquals(75, PlenitudeManaCost.calculate(100, 5));
        assertEquals(70, PlenitudeManaCost.calculate(100, 6));
        assertEquals(50, PlenitudeManaCost.calculate(100, 10));
        assertEquals(1, PlenitudeManaCost.calculate(100, 20));
        assertEquals(1, PlenitudeManaCost.calculate(100, 200));
    }

    @Test
    void preservesZeroAndRoundsPositiveCostsUp() {
        assertEquals(0, PlenitudeManaCost.calculate(0, 20));
        assertEquals(0, PlenitudeManaCost.calculate(-1, 20));
        assertEquals(2, PlenitudeManaCost.calculate(2, 1));
        assertEquals(29, PlenitudeManaCost.calculate(33, 3));
        assertEquals(100, PlenitudeManaCost.calculate(100, 0));
        assertEquals(100, PlenitudeManaCost.calculate(100, -1));
    }

}
