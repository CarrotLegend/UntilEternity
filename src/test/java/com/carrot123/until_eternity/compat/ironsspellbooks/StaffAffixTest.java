package com.carrot123.until_eternity.compat.ironsspellbooks;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffAffixTest {
    @Test
    void sevenStableIdsAndValuesAreDefined() {
        assertEquals(7, StaffAffix.values().length);
        assertEquals(Set.of("ancient", "newborn", "decadent", "refined",
                        "noble", "modest", "divine"),
                Arrays.stream(StaffAffix.values())
                        .map(StaffAffix::id).collect(java.util.stream.Collectors.toSet()));
        assertValues(StaffAffix.ANCIENT, 0.10D, -0.05D, 0.0D);
        assertValues(StaffAffix.NEWBORN, -0.05D, 0.10D, 0.0D);
        assertValues(StaffAffix.DECADENT, -0.05D, -0.05D, -0.05D);
        assertValues(StaffAffix.REFINED, 0.0D, 0.05D, 0.05D);
        assertValues(StaffAffix.NOBLE, 0.05D, -0.05D, 0.10D);
        assertValues(StaffAffix.MODEST, -0.10D, 0.20D, 0.0D);
        assertValues(StaffAffix.DIVINE, 0.15D, 0.10D, 0.20D);
        assertTrue(StaffAffix.byId("missing").isEmpty());
    }

    @Test
    void affixModifierIdsAreStableUniqueAndSeparateFromUpgradeIds() {
        Set<java.util.UUID> ids = new HashSet<>(Set.of(
                StaffAffixModifierIds.SPELL_POWER,
                StaffAffixModifierIds.CAST_TIME_REDUCTION,
                StaffAffixModifierIds.COOLDOWN_REDUCTION));
        assertEquals(3, ids.size());
        assertNotEquals(StaffAffixModifierIds.SPELL_POWER,
                StaffUpgradeModifierIds.SPELL_POWER);
        assertNotEquals(StaffAffixModifierIds.CAST_TIME_REDUCTION,
                StaffUpgradeModifierIds.CAST_TIME_REDUCTION);
        assertNotEquals(StaffAffixModifierIds.COOLDOWN_REDUCTION,
                StaffUpgradeModifierIds.COOLDOWN_REDUCTION);
    }

    @Test
    void upgradePrefixesAreCentralizedAndRejectInvalidLevels() {
        assertTrue(StaffAffixHelper.getUpgradePrefix(0).isEmpty());
        assertTrue(StaffAffixHelper.getUpgradePrefix(-1).isEmpty());
        assertTrue(StaffAffixHelper.getUpgradePrefix(6).isEmpty());
        assertEquals("staff_upgrade.until_eternity.common",
                StaffAffixHelper.getUpgradePrefix(1).orElseThrow().getString());
        assertEquals("staff_upgrade.until_eternity.legendary",
                StaffAffixHelper.getUpgradePrefix(5).orElseThrow().getString());
    }

    private static void assertValues(
            StaffAffix affix,
            double spellPower,
            double castTimeReduction,
            double cooldownReduction
    ) {
        assertEquals(spellPower, affix.spellPower());
        assertEquals(castTimeReduction, affix.castTimeReduction());
        assertEquals(cooldownReduction, affix.cooldownReduction());
    }
}
