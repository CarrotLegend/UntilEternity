package com.carrot123.until_eternity.item.curio;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class VoidRingItemTest {
    private static final UUID SLOT_UUID =
            UUID.fromString("12345678-1234-5678-9abc-def012345678");

    @Test
    void currentRevelationAttributeAmountsArePreserved() {
        assertEquals(2.0D, VoidRingItem.SPELL_POWER_AMOUNT);
        assertEquals(1.0D, VoidRingItem.SPELL_POWER_MULTIPLIER_AMOUNT);
        assertEquals(1, VoidRingItem.MAX_EQUIPPED);
    }

    @Test
    void modifierUuidIsStableForTheSameSlotAndKey() {
        UUID first = CurioModifierId.create(
                SLOT_UUID, "spell_power");
        UUID second = CurioModifierId.create(
                SLOT_UUID, "spell_power");

        assertEquals(first, second);
    }

    @Test
    void twoAttributeSaltsDoNotCollide() {
        UUID spellPower = CurioModifierId.create(
                SLOT_UUID, "spell_power");
        UUID multiplier = CurioModifierId.create(
                SLOT_UUID, "spell_power_multiplier");

        assertNotEquals(spellPower, multiplier);
    }

    @Test
    void exactDerivationInputIsSlotUuidAndModifierKey() {
        assertEquals(
                UUID.nameUUIDFromBytes((
                        SLOT_UUID + "/until_eternity/spell_power")
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                CurioModifierId.create(SLOT_UUID, "spell_power")
        );
    }

    @Test
    void differentSlotsDoNotCollide() {
        assertNotEquals(
                CurioModifierId.create(SLOT_UUID, "spell_power"),
                CurioModifierId.create(
                        UUID.fromString("87654321-4321-8765-cba9-876543210fed"),
                        "spell_power"));
    }
}
