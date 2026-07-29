package com.carrot123.until_eternity.item.curio;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CurioAttributeProfileTest {
    private static final UUID SLOT_UUID =
            UUID.fromString("12345678-1234-5678-9abc-def012345678");

    @Test
    void modifierKeysWithinEveryProfileAreUniqueForOneSlot() {
        for (CurioAttributeProfile profile : CurioAttributeProfile.values()) {
            long unique = profile.modifierSpecs().stream()
                    .map(spec -> CurioModifierId.create(
                            SLOT_UUID, spec.modifierKey()))
                    .distinct()
                    .count();
            assertEquals(profile.expectedModifierCount(), unique, profile.name());
        }
    }

    @Test
    void sameSlotAndKeyAreStable() {
        assertEquals(
                CurioModifierId.create(
                        SLOT_UUID, "attack_damage"),
                CurioModifierId.create(
                        SLOT_UUID, "attack_damage")
        );
    }

    @Test
    void sameKeyInDifferentSlotsDoesNotShareUuid() {
        assertNotEquals(
                CurioModifierId.create(
                        SLOT_UUID, "max_health"),
                CurioModifierId.create(
                        UUID.fromString("87654321-4321-8765-cba9-876543210fed"),
                        "max_health")
        );
    }

    @Test
    void profilesDeclareTheExpectedModifierCounts() {
        Map<CurioAttributeProfile, Integer> expectedCounts = Map.of(
                CurioAttributeProfile.ELEMENTAL_GAUNTLET, 4,
                CurioAttributeProfile.REAPER_TOOTH_NECKLACE, 2,
                CurioAttributeProfile.SAND_SHARK_TOOTH_NECKLACE, 2,
                CurioAttributeProfile.REGENERATOR, 2,
                CurioAttributeProfile.GUTTERING_CANDLE, 1,
                CurioAttributeProfile.EMPOWERED_SHIELD, 3,
                CurioAttributeProfile.COSMIC_AEGIS, 3,
                CurioAttributeProfile.PROOF_OF_SPURNER, 9
        );

        expectedCounts.forEach((profile, count) ->
                assertEquals(count.intValue(), profile.expectedModifierCount()));
    }

    @Test
    void proofOfSpurnerKeepsEveryModifierValueAndOperation() {
        Map<String, String> actual = CurioAttributeProfile.PROOF_OF_SPURNER
                .modifierSpecs().stream()
                .collect(Collectors.toMap(
                        CurioAttributeSpec::modifierKey,
                        spec -> spec.amount() + "/" + spec.operation()));

        assertEquals(Map.of(
                "attack_damage", "2.0/MULTIPLY_TOTAL",
                "attack_speed", "0.15/MULTIPLY_TOTAL",
                "knockback", "1.0/MULTIPLY_TOTAL",
                "max_health", "100.0/ADDITION",
                "armor", "8.0/ADDITION",
                "armor_toughness", "4.0/ADDITION",
                "damage_resistance", "0.7/MULTIPLY_TOTAL",
                "armor_penetration", "1.0/MULTIPLY_TOTAL",
                "enchantment_piercing", "0.5/MULTIPLY_TOTAL"
        ), actual);
    }
}
