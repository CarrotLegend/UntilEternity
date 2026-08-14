package com.carrot123.until_eternity.compat.ironsspellbooks;

import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class StaffUpgradeHelper {
    public static final String LEVEL_TAG = "LevelIs";
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;

    private StaffUpgradeHelper() {
    }

    public static boolean isUpgradeableStaff(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && (stack.is(IronSpellbookTags.STAFFS)
                || stack.getItem() instanceof StaffItem);
    }

    public static int getValidLevel(ItemStack stack) {
        if (!isUpgradeableStaff(stack)) {
            return 0;
        }
        return getValidStoredLevel(stack.getTag());
    }

    public static int getNextLevel(ItemStack stack) {
        if (!isUpgradeableStaff(stack)) {
            return 0;
        }
        return getNextStoredLevel(stack.getTag());
    }

    static int getValidStoredLevel(CompoundTag tag) {
        if (tag == null || !tag.contains(LEVEL_TAG, Tag.TAG_INT)) {
            return 0;
        }

        int level = tag.getInt(LEVEL_TAG);
        return level >= MIN_LEVEL && level <= MAX_LEVEL ? level : 0;
    }

    static int getNextStoredLevel(CompoundTag tag) {
        if (tag == null || !tag.contains(LEVEL_TAG)) {
            return MIN_LEVEL;
        }
        if (!tag.contains(LEVEL_TAG, Tag.TAG_INT)) {
            return 0;
        }

        int currentLevel = tag.getInt(LEVEL_TAG);
        return currentLevel >= MIN_LEVEL && currentLevel < MAX_LEVEL
                ? currentLevel + 1
                : 0;
    }
}
