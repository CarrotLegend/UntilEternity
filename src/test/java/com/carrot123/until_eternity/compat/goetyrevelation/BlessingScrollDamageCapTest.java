package com.carrot123.until_eternity.compat.goetyrevelation;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlessingScrollDamageCapTest {
    @Test
    void tiersAndNbtRemainIndependent() {
        CompoundTag first = new CompoundTag();
        CompoundTag second = new CompoundTag();
        assertEquals(0, BlessingScrollDamageCap.readTier(first));
        BlessingScrollDamageCap.writeTier(first, 2);
        assertEquals(2, BlessingScrollDamageCap.readTier(first));
        assertEquals(0, BlessingScrollDamageCap.readTier(second));
        assertNotEquals(cursedTag(), BlessingScrollDamageCap.CAP_TIER_TAG);
        BlessingScrollDamageCap.writeTier(first, 3);
        assertEquals(3, BlessingScrollDamageCap.readTier(first));
        first.putInt(cursedTag(), 4);
        assertEquals(3, BlessingScrollDamageCap.readTier(first));
        assertEquals(4, first.getInt(cursedTag()));
    }

    @Test
    void materialIdsAndUpgradeOrdering() {
        assertEquals(1, BlessingScrollDamageCap.getUpgradeTier(
                BlessingScrollDamageCap.DRAGONSKIN));
        assertEquals(2, BlessingScrollDamageCap.getUpgradeTier(
                BlessingScrollDamageCap.SHROUDED_BLUEPRINT));
        assertEquals(3, BlessingScrollDamageCap.getUpgradeTier(
                BlessingScrollDamageCap.UNHOLY_BLOOD));
        assertTrue(BlessingScrollDamageCap.canUpgrade(0, 3));
        assertFalse(BlessingScrollDamageCap.canUpgrade(2, 1));
        assertFalse(BlessingScrollDamageCap.canUpgrade(3, 3));
    }

    @Test
    void capsOnlyTheScrollsIncrement() {
        assertEquals(2.5D, BlessingScrollDamageCap.capOriginalAmountForTier(0, 1.0D, 2.5D));
        assertEquals(3.0D, BlessingScrollDamageCap.capOriginalAmountForTier(0, 1.0D, 4.0D));
        assertEquals(5.0D, BlessingScrollDamageCap.capOriginalAmountForTier(1, 1.0D, 6.0D));
        assertEquals(7.0D, BlessingScrollDamageCap.capOriginalAmountForTier(2, 1.0D, 8.0D));
        assertEquals(11.0D, BlessingScrollDamageCap.capOriginalAmountForTier(3, 1.0D, 11.0D));
        assertEquals(300.0D, BlessingScrollDamageCap.capOriginalAmountForTier(0, 100.0D, 1000.0D));
    }

    @Test
    void preservesZeroNegativeAndInvalidInputs() {
        assertEquals(0.0D, BlessingScrollDamageCap.capOriginalAmountForTier(0, 0.0D, 0.0D));
        assertEquals(0.5D, BlessingScrollDamageCap.capOriginalAmountForTier(0, 1.0D, 0.5D));
        assertEquals(Double.POSITIVE_INFINITY,
                BlessingScrollDamageCap.capOriginalAmountForTier(0, 1.0D,
                        Double.POSITIVE_INFINITY));
    }

    private static String cursedTag() {
        return "until_eternity:CursedScrollDamageCapTier";
    }
}
