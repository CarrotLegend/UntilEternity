package com.carrot123.until_eternity.enchantment;

import com.carrot123.until_eternity.compat.ironsspellbooks.IronSpellPowerCompat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmpowermentLogicTest {
    @Test
    void naturalMaximumAndUncappedRuntimeBonusesAreSeparate() {
        assertEquals(0.05D, EmpowermentLevel.bonusForLevel(1), 1.0E-9D);
        assertEquals(0.25D, EmpowermentLevel.bonusForLevel(5), 1.0E-9D);
        assertEquals(0.30D, EmpowermentLevel.bonusForLevel(6), 1.0E-9D);
        assertEquals(0.35D, EmpowermentLevel.bonusForLevel(7), 1.0E-9D);
        assertEquals(0.50D, EmpowermentLevel.bonusForLevel(10), 1.0E-9D);
        assertEquals(0.0D, EmpowermentLevel.bonusForLevel(0), 1.0E-9D);
        assertEquals(0.0D, EmpowermentLevel.bonusForLevel(-1), 1.0E-9D);
    }

    @Test
    void spellPowerUsesAdditiveEmpowermentAndCurrentMana() {
        assertEquals(
                1.85D,
                IronSpellPowerCompat.calculate(1.50D, 7, 0.0F, false),
                1.0E-9D);
        assertEquals(
                2.05D,
                IronSpellPowerCompat.calculate(1.50D, 7, 200.0F, true),
                1.0E-9D);
        assertEquals(
                1.50D,
                IronSpellPowerCompat.calculate(1.50D, 0, 200.0F, false),
                1.0E-9D);
    }

    @Test
    void invalidManaDoesNotCorruptSpellPower() {
        assertEquals(
                1.75D,
                IronSpellPowerCompat.calculate(
                        1.50D, 5, Float.NaN, true),
                1.0E-9D);
        assertEquals(
                1.50D,
                IronSpellPowerCompat.calculate(
                        1.50D, 0, -10.0F, true),
                1.0E-9D);
    }
}
