package com.carrot123.until_eternity.event;

public final class HorrorHuntDamageLogic {
    public static final int COOLDOWN_TICKS = 200;
    public static final float DAMAGE_MULTIPLIER = 10.0F;

    private HorrorHuntDamageLogic() {
    }

    public static boolean isReady(long gameTime, long nextProcGameTime) {
        return gameTime >= nextProcGameTime;
    }

    public static long nextProcGameTime(long gameTime) {
        return gameTime + COOLDOWN_TICKS;
    }

    public static float amplifiedDamage(float amount) {
        return amount * DAMAGE_MULTIPLIER;
    }
}
