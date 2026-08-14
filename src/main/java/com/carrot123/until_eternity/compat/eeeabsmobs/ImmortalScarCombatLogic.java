package com.carrot123.until_eternity.compat.eeeabsmobs;

public final class ImmortalScarCombatLogic {
    public static final float APPLICATION_CHANCE = 0.5F;
    public static final int DURATION_TICKS = 200;
    public static final int AMPLIFIER = 0;
    public static final float DAMAGE_MULTIPLIER = 2.0F;

    private ImmortalScarCombatLogic() {
    }

    public static boolean shouldApply(float roll) {
        return Float.isFinite(roll) && roll >= 0.0F
                && roll < APPLICATION_CHANCE;
    }

    public static float doubleDamage(float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0F) {
            return amount;
        }
        return (float) Math.min((double) amount * DAMAGE_MULTIPLIER,
                Float.MAX_VALUE);
    }
}
