package com.carrot123.until_eternity.trial;

public enum TrialDifficulty {
    EASY(2.0, 1.0),
    NORMAL(4.0, 1.0),
    HARD(6.0, 1.5),
    MALICE(10.0, 3.0);

    private final double healthMultiplier;
    private final double damageMultiplier;

    TrialDifficulty(double healthMultiplier, double damageMultiplier) {
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
    }

    public double healthMultiplier() { return healthMultiplier; }
    public double damageMultiplier() { return damageMultiplier; }
    public static TrialDifficulty byNetworkId(int id) {
        return id >= 0 && id < values().length ? values()[id] : null;
    }

    public static TrialDifficulty fromSaved(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException | NullPointerException ignored) { return NORMAL; }
    }
}
