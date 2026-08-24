package com.carrot123.until_eternity.combat;

public final class CookingFrenzyProgression {
    public static final int DURATION_TICKS = 200;
    public static final int MAX_AMPLIFIER = 9;

    private CookingFrenzyProgression() {
    }

    public static int nextAmplifier(int currentAmplifier) {
        return currentAmplifier < 0
                ? 0
                : Math.min(currentAmplifier + 1, MAX_AMPLIFIER);
    }
}
