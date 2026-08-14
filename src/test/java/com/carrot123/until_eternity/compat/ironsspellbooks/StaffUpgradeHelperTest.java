package com.carrot123.until_eternity.compat.ironsspellbooks;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaffUpgradeHelperTest {
    @Test
    void readsOnlyIntegerLevelsOneThroughFive() {
        assertEquals(0, StaffUpgradeHelper.getValidStoredLevel(null));

        for (int level = 1; level <= 5; level++) {
            CompoundTag tag = integerLevel(level);
            assertEquals(
                    level,
                    StaffUpgradeHelper.getValidStoredLevel(tag));
        }

        for (int level : new int[]{-1, 0, 6, 999}) {
            assertEquals(
                    0,
                    StaffUpgradeHelper.getValidStoredLevel(
                            integerLevel(level)));
        }
    }

    @Test
    void rejectsWrongNbtTypes() {
        CompoundTag stringLevel = new CompoundTag();
        stringLevel.putString(StaffUpgradeHelper.LEVEL_TAG, "3");
        CompoundTag shortLevel = new CompoundTag();
        shortLevel.putShort(StaffUpgradeHelper.LEVEL_TAG, (short) 3);
        CompoundTag doubleLevel = new CompoundTag();
        doubleLevel.putDouble(StaffUpgradeHelper.LEVEL_TAG, 3.0D);

        assertEquals(
                0,
                StaffUpgradeHelper.getValidStoredLevel(stringLevel));
        assertEquals(
                0,
                StaffUpgradeHelper.getValidStoredLevel(shortLevel));
        assertEquals(
                0,
                StaffUpgradeHelper.getValidStoredLevel(doubleLevel));
    }

    @Test
    void nextLevelRequiresAnAbsentKeyOrAValidLevelBelowFive() {
        assertEquals(1, StaffUpgradeHelper.getNextStoredLevel(null));
        assertEquals(
                1,
                StaffUpgradeHelper.getNextStoredLevel(new CompoundTag()));

        for (int current = 1; current < 5; current++) {
            assertEquals(
                    current + 1,
                    StaffUpgradeHelper.getNextStoredLevel(
                            integerLevel(current)));
        }

        for (int level : new int[]{-1, 0, 5, 6, 999}) {
            assertEquals(
                    0,
                    StaffUpgradeHelper.getNextStoredLevel(
                            integerLevel(level)));
        }

        CompoundTag wrongType = new CompoundTag();
        wrongType.putString(StaffUpgradeHelper.LEVEL_TAG, "1");
        assertEquals(
                0,
                StaffUpgradeHelper.getNextStoredLevel(wrongType));
    }

    private static CompoundTag integerLevel(int level) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(StaffUpgradeHelper.LEVEL_TAG, level);
        return tag;
    }
}
