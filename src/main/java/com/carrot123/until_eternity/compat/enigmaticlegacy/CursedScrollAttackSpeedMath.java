package com.carrot123.until_eternity.compat.enigmaticlegacy;

public final class CursedScrollAttackSpeedMath {
    private CursedScrollAttackSpeedMath() {
    }

    public static double calculate(double perCurse, int curseCount) {
        if (!Double.isFinite(perCurse) || perCurse <= 0.0D || curseCount <= 0) {
            return 0.0D;
        }
        double result = perCurse * curseCount;
        return Double.isFinite(result) && result > 0.0D ? result : 0.0D;
    }
}
