package com.carrot123.until_eternity.effect;

public final class VoidCorrosionDamageLogic {
    private static final float DAMAGE_MULTIPLIER = 2.0F;
    private static final float MAX_HEALTH_FRACTION = 0.01F;
    private static final float MINIMUM_REMAINING_HEALTH = 1.0F;

    private VoidCorrosionDamageLogic() {
    }

    public static float amplifyIncomingDamage(float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0F) {
            return amount;
        }
        float amplified = amount * DAMAGE_MULTIPLIER;
        return Float.isFinite(amplified) ? amplified : amount;
    }

    public static float periodicDamage(float maxHealth, float health) {
        if (!Float.isFinite(maxHealth)
                || !Float.isFinite(health)
                || maxHealth <= 0.0F
                || health <= MINIMUM_REMAINING_HEALTH) {
            return 0.0F;
        }
        float intended = maxHealth * MAX_HEALTH_FRACTION;
        float maxDamage = Math.max(0.0F, health - MINIMUM_REMAINING_HEALTH);
        return Math.min(intended, maxDamage);
    }
}
