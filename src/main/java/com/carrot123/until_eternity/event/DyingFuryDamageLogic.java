package com.carrot123.until_eternity.event;

final class DyingFuryDamageLogic {
    static final double BONUS_PER_MISSING_HEALTH = 0.002D;

    private DyingFuryDamageLogic() {
    }

    static double calculateLostHealth(float maxHealth, float currentHealth) {
        if (!Float.isFinite(maxHealth)
                || !Float.isFinite(currentHealth)
                || maxHealth <= 0.0F) {
            return 0.0D;
        }

        double clampedHealth = Math.max(
                0.0D,
                Math.min((double) currentHealth, (double) maxHealth));
        return Math.max(0.0D, (double) maxHealth - clampedHealth);
    }

    static double modifierAmount(float maxHealth, float currentHealth) {
        return calculateLostHealth(maxHealth, currentHealth)
                * BONUS_PER_MISSING_HEALTH;
    }
}
