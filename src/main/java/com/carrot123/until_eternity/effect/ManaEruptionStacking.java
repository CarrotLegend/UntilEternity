package com.carrot123.until_eternity.effect;

public final class ManaEruptionStacking {
    public static final int MAX_EFFECT_LEVEL = 10;
    public static final int MAX_AMPLIFIER = MAX_EFFECT_LEVEL - 1;

    private ManaEruptionStacking() {
    }

    public static int clampAmplifier(int amplifier) {
        return Math.max(0, Math.min(MAX_AMPLIFIER, amplifier));
    }

    public static int mergeAmplifier(int currentAmplifier, int incomingAmplifier) {
        int current = clampAmplifier(currentAmplifier);
        int incoming = clampAmplifier(incomingAmplifier);
        return Math.min(MAX_AMPLIFIER, Math.max(current + 1, incoming));
    }

    public static int mergeDuration(int currentDuration, int incomingDuration) {
        return Math.max(currentDuration, incomingDuration);
    }

    public static double attributeAmount(int amplifier) {
        return ATTRIBUTE_AMOUNT_PER_LEVEL * (clampAmplifier(amplifier) + 1);
    }

    public static double focusDamageAmount(int amplifier) {
        return FOCUS_DAMAGE_AMOUNT_PER_LEVEL
                * (clampAmplifier(amplifier) + 1);
    }

    private static final double ATTRIBUTE_AMOUNT_PER_LEVEL =
            ManaEruptionEffect.ATTRIBUTE_AMOUNT_PER_LEVEL;
    private static final double FOCUS_DAMAGE_AMOUNT_PER_LEVEL =
            ManaEruptionEffect.FOCUS_DAMAGE_AMOUNT_PER_LEVEL;
}
