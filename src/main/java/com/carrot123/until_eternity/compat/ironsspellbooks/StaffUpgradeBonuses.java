package com.carrot123.until_eternity.compat.ironsspellbooks;

public record StaffUpgradeBonuses(
        double spellPower,
        double cooldownReduction,
        double castTimeReduction
) {
    public static StaffUpgradeBonuses forLevel(int level) {
        if (level < StaffUpgradeHelper.MIN_LEVEL
                || level > StaffUpgradeHelper.MAX_LEVEL) {
            return new StaffUpgradeBonuses(0.0D, 0.0D, 0.0D);
        }
        return new StaffUpgradeBonuses(
                level,
                level * 0.05D,
                level * 0.05D);
    }
}
