package com.carrot123.until_eternity.compat.ironsspellbooks;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaffUpgradeBonusesTest {
    @Test
    void levelsOneThroughFiveProduceTheSpecifiedAdditions() {
        for (int level = 1; level <= 5; level++) {
            StaffUpgradeBonuses bonuses =
                    StaffUpgradeBonuses.forLevel(level);
            assertEquals(level, bonuses.spellPower(), 0.0D);
            assertEquals(
                    level * 0.05D,
                    bonuses.cooldownReduction(),
                    0.0D);
            assertEquals(
                    level * 0.05D,
                    bonuses.castTimeReduction(),
                    0.0D);
        }

        assertEquals(
                new StaffUpgradeBonuses(0.0D, 0.0D, 0.0D),
                StaffUpgradeBonuses.forLevel(0));
        assertEquals(
                new StaffUpgradeBonuses(0.0D, 0.0D, 0.0D),
                StaffUpgradeBonuses.forLevel(6));
    }

    @Test
    void modifierUuidsUseStableDistinctSeeds() {
        assertEquals(
                expected("spell_power"),
                StaffUpgradeModifierIds.SPELL_POWER);
        assertEquals(
                expected("cooldown_reduction"),
                StaffUpgradeModifierIds.COOLDOWN_REDUCTION);
        assertEquals(
                expected("cast_time_reduction"),
                StaffUpgradeModifierIds.CAST_TIME_REDUCTION);
        assertEquals(
                3,
                new HashSet<>(Set.of(
                        StaffUpgradeModifierIds.SPELL_POWER,
                        StaffUpgradeModifierIds.COOLDOWN_REDUCTION,
                        StaffUpgradeModifierIds.CAST_TIME_REDUCTION))
                        .size());
    }

    private static UUID expected(String modifierKey) {
        return UUID.nameUUIDFromBytes(
                ("until_eternity:staff_upgrade/" + modifierKey)
                        .getBytes(StandardCharsets.UTF_8));
    }
}
