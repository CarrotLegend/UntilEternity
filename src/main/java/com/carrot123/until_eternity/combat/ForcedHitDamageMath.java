package com.carrot123.until_eternity.combat;

public final class ForcedHitDamageMath {
    private ForcedHitDamageMath() {
    }

    public static float preservePositiveHookResult(float input, float hookResult) {
        return Float.isFinite(hookResult) && hookResult > 0.0F ? hookResult : input;
    }
}
