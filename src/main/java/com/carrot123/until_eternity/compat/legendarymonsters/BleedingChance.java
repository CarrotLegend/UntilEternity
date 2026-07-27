package com.carrot123.until_eternity.compat.legendarymonsters;

public final class BleedingChance {
    private BleedingChance() {
    }

    public static boolean shouldApply(float roll) {
        return roll < 0.5F;
    }
}
