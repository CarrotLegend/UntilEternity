package com.carrot123.until_eternity.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ReplicaGelItemTest {
    @Test
    void firstAndPenultimateCraftsAdvanceOnePoint() {
        assertEquals(1, next(0));
        assertEquals(1023, next(1022));
    }

    @Test
    void damage1023CompletesTheLastCraftThenBreaks() {
        assertEquals(ReplicaGelCraftingDurability.BROKEN, next(1023));
    }

    @Test
    void freshGelParticipatesInExactly1024Crafts() {
        int damage = 0;
        for (int craft = 1;
             craft <= ReplicaGelItem.MAX_DURABILITY;
             craft++) {
            int nextDamage = next(damage);
            if (craft < ReplicaGelItem.MAX_DURABILITY) {
                assertNotEquals(ReplicaGelCraftingDurability.BROKEN,
                        nextDamage, "craft " + craft);
                assertEquals(craft, nextDamage, "craft " + craft);
                damage = nextDamage;
            } else {
                assertEquals(ReplicaGelCraftingDurability.BROKEN,
                        nextDamage, "craft " + craft);
            }
        }
    }

    private static int next(int damage) {
        return ReplicaGelCraftingDurability.nextDamageOrBroken(
                damage, ReplicaGelItem.MAX_DURABILITY);
    }
}
