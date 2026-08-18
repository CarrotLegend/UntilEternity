package com.carrot123.until_eternity.util;

public final class NaturalHealingMath {
    private static final double PERCENT_SCALE = 0.01D;

    private NaturalHealingMath() {
    }

    public static float apply(float originalAmount, double natureHeal) {
        return (float) (originalAmount
                * (1.0D + natureHeal * PERCENT_SCALE));
    }
}
