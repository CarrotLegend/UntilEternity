package com.carrot123.until_eternity.combat;

public final class AbsoluteDamageMath {
    private AbsoluteDamageMath() {
    }

    public static Split split(float damage, float health, boolean alive) {
        if (!Float.isFinite(damage) || damage <= 0.0F
                || !Float.isFinite(health) || !alive || health <= 1.0F) {
            return new Split(0.0F, damage);
        }

        float desiredDirect = damage * 0.5F;
        float maxDirectWithoutKilling = Math.max(0.0F, health - 1.0F);
        float actualDirect = Math.min(desiredDirect, maxDirectWithoutKilling);
        return new Split(actualDirect, damage - actualDirect);
    }

    public record Split(float directDamage, float eventDamage) {
    }
}
