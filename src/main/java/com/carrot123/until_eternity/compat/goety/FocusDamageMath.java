package com.carrot123.until_eternity.compat.goety;

public final class FocusDamageMath {
    private FocusDamageMath() {
    }

    public static float apply(float originalDamage, double multiplier) {
        if (!(originalDamage > 0.0F)
                || !Float.isFinite(originalDamage)
                || !(multiplier > 0.0D)
                || !Double.isFinite(multiplier)) {
            return originalDamage;
        }
        double result = originalDamage * multiplier;
        if (!Double.isFinite(result) || result > Float.MAX_VALUE) {
            return originalDamage;
        }
        return (float) result;
    }
}
