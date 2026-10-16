package com.carrot123.until_eternity.trial;

public enum TrialDifficulty {
    EASY(2.0, 1.2, 1.0, 1),
    NORMAL(4.0, 1.5, 1.0, 2),
    HARD(6.0, 2.0, 1.5, 3),
    MALICE(10.0, 4.0, 3.0, 4);

    private final double healthMultiplier;
    private final double armorMultiplier;
    private final double damageMultiplier;
    private final int rewardCount;

    TrialDifficulty(double healthMultiplier, double armorMultiplier, double damageMultiplier, int rewardCount) {
        this.healthMultiplier = healthMultiplier;
        this.armorMultiplier = armorMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.rewardCount = rewardCount;
    }

    public double healthMultiplier() { return healthMultiplier; }
    public double armorMultiplier() { return armorMultiplier; }
    public double damageMultiplier() { return damageMultiplier; }
    public int rewardCount() { return rewardCount; }

    public static TrialDifficulty byNetworkId(int id) {
        return id >= 0 && id < values().length ? values()[id] : null;
    }

    public static TrialDifficulty fromSaved(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException | NullPointerException ignored) { return NORMAL; }
    }
}