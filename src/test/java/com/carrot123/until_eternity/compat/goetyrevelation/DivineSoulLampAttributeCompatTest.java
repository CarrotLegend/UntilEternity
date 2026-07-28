package com.carrot123.until_eternity.compat.goetyrevelation;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DivineSoulLampAttributeCompatTest {
    private static final UUID FIRST_SLOT =
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID SECOND_SLOT =
            UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Test
    void derivationIsStableForTheSameSlotAndSalt() {
        UUID first = DivineSoulLampAttributeCompat.deriveModifierUuid(
                FIRST_SLOT,
                DivineSoulLampAttributeCompat.SPELL_POWER_FLAT_SALT
        );
        UUID second = DivineSoulLampAttributeCompat.deriveModifierUuid(
                FIRST_SLOT,
                DivineSoulLampAttributeCompat.SPELL_POWER_FLAT_SALT
        );

        assertEquals(first, second);
    }

    @Test
    void differentSaltsDoNotCollideWithinOneSlot() {
        Set<UUID> modifierUuids = Stream.of(
                        DivineSoulLampAttributeCompat.SOUL_REFLUX_SALT,
                        DivineSoulLampAttributeCompat.SOUL_AFFINITY_SALT,
                        DivineSoulLampAttributeCompat.SPELL_POWER_FLAT_SALT,
                        DivineSoulLampAttributeCompat.SPELL_POWER_PERCENT_SALT,
                        DivineSoulLampAttributeCompat.SPELL_POWER_MULTIPLIER_SALT
                )
                .map(salt -> DivineSoulLampAttributeCompat.deriveModifierUuid(FIRST_SLOT, salt))
                .collect(Collectors.toSet());

        assertEquals(5, modifierUuids.size());
    }

    @Test
    void differentSlotsDoNotShareModifierUuids() {
        UUID first = DivineSoulLampAttributeCompat.deriveModifierUuid(
                FIRST_SLOT,
                DivineSoulLampAttributeCompat.SOUL_REFLUX_SALT
        );
        UUID second = DivineSoulLampAttributeCompat.deriveModifierUuid(
                SECOND_SLOT,
                DivineSoulLampAttributeCompat.SOUL_REFLUX_SALT
        );

        assertNotEquals(first, second);
    }
}
