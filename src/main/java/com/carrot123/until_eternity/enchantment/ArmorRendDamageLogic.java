package com.carrot123.until_eternity.enchantment;

public final class ArmorRendDamageLogic {
    private static final int MINIMUM_ARMOR = 6;
    private static final float DAMAGE_PER_LEVEL = 0.10F;

    private ArmorRendDamageLogic() {
    }

    public static float apply(float amount, int armor, int actualLevel) {
        if (!Float.isFinite(amount) || amount <= 0.0F
                || armor < MINIMUM_ARMOR || actualLevel <= 0) {
            return amount;
        }
        float result = amount * (1.0F + DAMAGE_PER_LEVEL * actualLevel);
        return Float.isFinite(result) ? result : amount;
    }
}
