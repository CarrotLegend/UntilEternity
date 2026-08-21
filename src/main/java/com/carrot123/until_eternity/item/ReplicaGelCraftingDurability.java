package com.carrot123.until_eternity.item;

final class ReplicaGelCraftingDurability {
    static final int BROKEN = -1;

    private ReplicaGelCraftingDurability() {
    }

    static int nextDamageOrBroken(int currentDamage, int maxDamage) {
        int nextDamage = currentDamage + 1;
        return nextDamage >= maxDamage ? BROKEN : nextDamage;
    }
}
