package com.carrot123.until_eternity.effect;

public final class VoidCorrosionDamageLogic {
    private static final float DAMAGE_MULTIPLIER = 2.0F;
    private static final float MAX_HEALTH_FRACTION = 0.01F;

    private VoidCorrosionDamageLogic() {
    }

    public static float amplifyIncomingDamage(float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0F) {
            return amount;
        }
        float amplified = amount * DAMAGE_MULTIPLIER;
        return Float.isFinite(amplified) ? amplified : amount;
    }

    public static float periodicDamage(float maxHealth) {
        if (!Float.isFinite(maxHealth) || maxHealth <= 0.0F) {
            return 0.0F;
        }
        float amount = maxHealth * MAX_HEALTH_FRACTION;
        return Float.isFinite(amount) ? amount : 0.0F;
    }
}
